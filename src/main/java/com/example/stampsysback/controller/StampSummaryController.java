package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.sse.StampSummaryEmitterService;
import com.example.stampsysback.service.StampSummaryService;
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
    private final StampSummaryEmitterService emitterService;

    public StampSummaryController(StampSummaryService service, StampSummaryEmitterService emitterService) {
        this.service = service;
        this.emitterService = emitterService;
    }

    @GetMapping("/{roomId}/stamp-summary")
    public ResponseEntity<List<StampSummary>> getStampSummary(@PathVariable Long roomId) {
        List<StampSummary> list = service.getStampDistributionForRoom(roomId);
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/{roomId}/stamp-summary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStampSummary(@PathVariable Long roomId) {
        // 0L にして無制限接続にするか、適切なタイムアウト(ms)を指定
        long timeout = 0L;
        return emitterService.createEmitter(roomId, timeout);
    }
}