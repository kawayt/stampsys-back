package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampActivityResponse;

import java.time.OffsetDateTime;

public interface StampActivityService {
    /**
     * Build timeline + series response for given room.
     *
     * @param roomId      room id
     * @param intervalStr interval string, validated by controller (e.g. "5 minutes")
     * @param startTs     optional start timestamp (nullable)
     * @param endTs       optional end timestamp (nullable)
     * @return StampActivityResponse (timeline aligned)
     */
    StampActivityResponse getStampActivity(Long roomId, String intervalStr, OffsetDateTime startTs, OffsetDateTime endTs);
}