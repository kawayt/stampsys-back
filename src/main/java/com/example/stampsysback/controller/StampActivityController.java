package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampActivityResponse;
import com.example.stampsysback.service.StampActivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Set;

/*
 * Controller exposing stamp activity endpoint.
 * GET /api/rooms/{roomId}/stamp-activity?interval=5 minutes&start=...&end=...
 */
@RestController
@RequestMapping("/api/rooms")
public class StampActivityController {

    private final StampActivityService service;

    // whitelist of allowed interval strings (server-side validation)
    private static final Set<String> ALLOWED_INTERVALS = Set.of(
            "1 minute", "5 minutes", "15 minutes", "30 minutes", "1 hour"
    );

    public StampActivityController(StampActivityService service) {
        this.service = service;
    }

    @GetMapping("/{roomId}/stamp-activity")
    public ResponseEntity<StampActivityResponse> getStampActivity(
            @PathVariable("roomId") Long roomId,
            @RequestParam(name = "interval", defaultValue = "5 minutes") String interval,
            @RequestParam(name = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(name = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        // Validate interval against whitelist
        if (!ALLOWED_INTERVALS.contains(interval)) {
            return ResponseEntity.badRequest().build();
        }
        StampActivityResponse resp = service.getStampActivity(roomId, interval, start, end);
        return ResponseEntity.ok(resp);
    }
}