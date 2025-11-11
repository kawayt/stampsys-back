package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.mapper.StampSendRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StampSendServiceImpl implements StampSendService {

    private final StampSendRecordMapper stampSendRecordMapper;

    public StampSendServiceImpl(StampSendRecordMapper stampSendRecordMapper) {
        this.stampSendRecordMapper = stampSendRecordMapper;
    }

    @Override
    @Transactional
    public StampSendRecord saveStamp(Integer userId, StampSendRequest stampSendRequest) {
        StampSendRecord record = new StampSendRecord();
        record.setUserId(userId);
        record.setStampId(stampSendRequest.getStampId());
        record.setRoomId(stampSendRequest.getRoomId());
        // createdAt はデフォルトで現在時刻が入るようにしている
        stampSendRecordMapper.insertStampSendRecord(record);
        return record;
    }
}
