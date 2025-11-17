package com.example.stampsysback.dto;

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
public class StampActivityResponse {
    private List<OffsetDateTime> timeline;
    private List<Integer> totals;
    private List<StampSeries> series;

    public List<OffsetDateTime> getTimeline() { return timeline; }
    public void setTimeline(List<OffsetDateTime> timeline) { this.timeline = timeline; }

    public List<Integer> getTotals() { return totals; }
    public void setTotals(List<Integer> totals) { this.totals = totals; }

    public List<StampSeries> getSeries() { return series; }
    public void setSeries(List<StampSeries> series) { this.series = series; }
}
