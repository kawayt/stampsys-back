package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.StampLogDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Mapper for stamp_logs queries that include sender name.
 */
@Mapper
public interface StampLogMapper {
    List<StampLogDto> findByRoomIdWithSender(
            @Param("roomId") Long roomId,
            @Param("startTs") OffsetDateTime startTs,
            @Param("endTs") OffsetDateTime endTs,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}