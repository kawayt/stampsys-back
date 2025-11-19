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
        //StampDisplayMapperのSELECT文で取得した、
        //「入室中のルームと同じclass_idを持つstamps_classesテーブルのstamp_idに紐づいたstamp_name,stamp_color,stamp_icon」を返す
        return stampDisplayMapper.findStampsByRoomId(roomId);
    }
}
