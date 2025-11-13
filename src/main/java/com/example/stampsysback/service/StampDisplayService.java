package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampDto;
import com.example.stampsysback.mapper.StampDisplayMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StampDisplayService {
    private final StampDisplayMapper stampDisplayMapper;

    public StampDisplayService(StampDisplayMapper stampDisplayMapper) {
        this.stampDisplayMapper = stampDisplayMapper;
    }

    public List<StampDto> findStampsByRoomId(Integer roomId) {
        return stampDisplayMapper.findStampsByRoomId(roomId);
    }

    public List<StampDto> findStampsByClassId(Integer classId) {
        return stampDisplayMapper.findStampsByClassId(classId);
    }
}
