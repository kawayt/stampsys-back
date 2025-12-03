package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.service.StampSummaryService;
import com.example.stampsysback.sse.StampSummaryEmitterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSummaryController {

    private final StampSummaryService service;
    private final StampSummaryEmitterRegistry emitterRegistry;

    public StampSummaryController(StampSummaryService service, StampSummaryEmitterRegistry emitterRegistry) {
        this.service = service;
        this.emitterRegistry = emitterRegistry;
    }

    // 既存の同期取得はそのまま
    @GetMapping("/{roomId}/stamp-summary")
    public ResponseEntity<List<StampSummary>> getStampSummary(@PathVariable Long roomId) {
        List<StampSummary> list = service.getStampDistributionForRoom(roomId);
        return ResponseEntity.ok(list);
    }

    // SSE: 接続を登録し、接続直後に初回スナップショットを送る（以降はイベント駆動で通知される）
    @GetMapping(path = "/{roomId}/stamp-summary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStampSummary(@PathVariable Long roomId) {
        SseEmitter emitter = emitterRegistry.register(roomId);

        // 接続直後に初回のスナップショットを送る（失敗しても接続は維持）
        try {
            List<StampSummary> list = service.getStampDistributionForRoom(roomId);
            emitter.send(SseEmitter.event().name("summary").data(list, MediaType.APPLICATION_JSON));
        } catch (Exception ex) {
            // 初回送信に失敗しても接続自体は維持する
        }

        return emitter;
    }
}