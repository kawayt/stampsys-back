package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampLogDto;
import com.example.stampsysback.mapper.StampLogMapper;
import com.example.stampsysback.service.StampLogService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * シンプル実装: Mapper の JOIN クエリを呼ぶだけ。
 * StampLogDto は mapper の resultMap で senderName を含むようにマッピングされている前提。
 */
@Service
public class StampLogServiceImpl implements StampLogService {

    private final StampLogMapper mapper;

    public StampLogServiceImpl(StampLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StampLogDto> getStampLogs(Long roomId, OffsetDateTime startTs, OffsetDateTime endTs, Integer limit, Integer offset) {
        if (roomId == null) return List.of();
        // 入力検証や limit 上限チェックをここで行う（必要なら）
        return mapper.findByRoomIdWithSender(roomId, startTs, endTs, limit, offset);
    }
}