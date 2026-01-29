package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.service.StampSummaryService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/rooms")
public class StampSummaryController {

    private final StampSummaryService service;
    private final TaskExecutor sseTaskExecutor;

    public StampSummaryController(StampSummaryService service, TaskExecutor sseTaskExecutor) {
        this.service = service;
        this.sseTaskExecutor = sseTaskExecutor;
    }

    @GetMapping("/{roomId}/stamp-summary")
    public ResponseEntity<List<StampSummary>> getStampSummary(@PathVariable Long roomId) {
        List<StampSummary> list = service.getStampDistributionForRoom(roomId);
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/{roomId}/stamp-summary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStampSummary(@PathVariable Long roomId) {
        final SseEmitter emitter = new SseEmitter(0L);
        final AtomicBoolean running = new AtomicBoolean(true);

        // エミッター終了時にループを止めるハンドラ
        emitter.onCompletion(() -> running.set(false));
        emitter.onTimeout(() -> running.set(false));
        emitter.onError((err) -> running.set(false));

        // 共有の TaskExecutor を使ってタスクを実行（ExecutorService を毎回作らない）
        sseTaskExecutor.execute(() -> {
            try {
                while (running.get() && !Thread.currentThread().isInterrupted()) {
                    List<StampSummary> list = service.getStampDistributionForRoom(roomId);
                    emitter.send(SseEmitter.event()
                            .name("summary")
                            .data(list, MediaType.APPLICATION_JSON));
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                // ループを抜けたら完了を通知
                emitter.complete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }
}