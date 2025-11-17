package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.mapper.StampSummaryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StampSummaryService {
    private final StampSummaryMapper stampSummaryMapper;

    public StampSummaryService(StampSummaryMapper mapper) {
        this.stampSummaryMapper = mapper;
    }

    public List<StampSummary> getStampDistributionForRoom(Long roomId) {
        return stampSummaryMapper.findLatestStampDistributionByRoomId(roomId);
    }
}