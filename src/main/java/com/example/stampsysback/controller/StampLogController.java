package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampLogDto;
import com.example.stampsysback.service.StampLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * GET /api/rooms/{roomId}/stamp-logs
 * query: start, end, limit, offset
 */
@RestController
@RequestMapping("/api/rooms")
public class StampLogController {

    private final StampLogService service;

    public StampLogController(StampLogService service) {
        this.service = service;
    }

    @GetMapping("/{roomId}/stamp-logs")
    public ResponseEntity<List<StampLogDto>> getStampLogs(
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "offset", required = false) Integer offset
    ) {
        // TODO: 認可チェックを実装する（呼び出しユーザが room のデータにアクセスできるか）
        // TODO: limit の上限（例: 1000）を設ける
        List<StampLogDto> rows = service.getStampLogs(roomId, start, end, limit, offset);
        return ResponseEntity.ok(rows);
    }
}