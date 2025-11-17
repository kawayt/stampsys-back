package com.example.stampsysback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Mapper for bucketed stamp activity (time-series) per room.
 * Returns a flat list of maps with keys:
 *  - ts: OffsetDateTime (bucket start)
 *  - stampId: Integer
 *  - cnt: Integer
 *  - total_cnt: Integer
 *  - pct: Number
 * The XML mapper (StampActivityMapper.xml) does the SQL and returns resultType="map".
 */
@Mapper
public interface StampActivityMapper {
    /**
     * Returns bucketed stamp counts for the given room.
     *
     * @param roomId      room id to aggregate
     * @param intervalStr bucket interval string (e.g. "5 minutes") — validate/whitelist on controller side
     * @param startTs     optional start timestamp (nullable). If null, SQL uses MIN(sent_at)
     * @param endTs       optional end timestamp (nullable). If null, SQL uses MAX(sent_at)
     * @return list of maps representing (ts, stampId, cnt, total_cnt, pct)
     */
    List<Map<String, Object>> findBucketedStampCounts(
            @Param("roomId") Long roomId,
            @Param("intervalStr") String intervalStr,
            @Param("startTs") OffsetDateTime startTs,
            @Param("endTs") OffsetDateTime endTs
    );
}