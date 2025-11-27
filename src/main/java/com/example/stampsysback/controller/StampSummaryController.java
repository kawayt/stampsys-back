package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.service.StampSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSummaryController {

    private final StampSummaryService service;

    public StampSummaryController(StampSummaryService service) {
        this.service = service;
    }

    // 教員画面用: 各ユーザーの最新スタンプに基づく割合を取得
    @GetMapping("/{roomId}/stamp-summary")
    public ResponseEntity<List<StampSummary>> getStampSummary(@PathVariable Long roomId) {
        List<StampSummary> list = service.getStampDistributionForRoom(roomId);
        return ResponseEntity.ok(list);
    }
}