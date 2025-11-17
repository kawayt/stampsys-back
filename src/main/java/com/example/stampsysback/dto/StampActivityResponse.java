package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response DTO returned to front-end:
 * {
 *   timeline: [ "2025-11-14T10:00:00+09:00", ... ],
 *   totals: [3, 2, ...],
 *   series: [ StampSeries, ... ]
 * }
 */

@Getter
@Setter
public class StampActivityResponse {
    private List<OffsetDateTime> timeline;
    private List<Integer> totals;
    private List<StampSeries> series;
}
