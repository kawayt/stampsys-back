package com.example.stampsysback.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * roomId ごとに接続中の SseEmitter を管理し、ブロードキャストするレジストリ。
 */
@Component
public class StampSummaryEmitterRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 指定ルームに対する新しい SseEmitter を登録して返す。
     * タイムアウトは無期限（0L）。必要に応じて調整してください。
     */
    public SseEmitter register(Long roomId) {
        final SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>()).add(emitter);

        // クリーンアップ
        emitter.onCompletion(() -> removeEmitter(roomId, emitter));
        emitter.onTimeout(() -> removeEmitter(roomId, emitter));
        emitter.onError((e) -> removeEmitter(roomId, emitter));

        return emitter;
    }

    private void removeEmitter(Long roomId, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(roomId);
        if (set == null) return;
        set.remove(emitter);
        if (set.isEmpty()) {
            emitters.remove(roomId);
        }
    }

    /**
     * 指定ルームの全接続に対して summary イベントを送信する。
     * 送信に失敗した emitter はレジストリから削除する（接続切断想定）。
     */
    public void broadcast(Long roomId, Object data) {
        Set<SseEmitter> set = emitters.get(roomId);
        if (set == null || set.isEmpty()) return;

        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event().name("summary").data(data));
            } catch (Exception ex) {
                // 送れなければそのコネクションを削除
                removeEmitter(roomId, emitter);
            }
        }
    }
}