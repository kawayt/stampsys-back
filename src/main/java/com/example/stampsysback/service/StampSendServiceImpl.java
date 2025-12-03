package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.event.StampLogCreatedEvent;
import com.example.stampsysback.mapper.RoomMapper;
import com.example.stampsysback.mapper.StampSendRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// ビジネスロジック (DTO -> Entity を生成し、Mapper を呼び出す)
@Service
public class StampSendServiceImpl implements StampSendService {

    private static final Logger logger = LoggerFactory.getLogger(StampSendServiceImpl.class);

    private final StampSendRecordMapper stampSendRecordMapper;
    private final RoomMapper roomMapper;
    private final ApplicationEventPublisher eventPublisher;

    public StampSendServiceImpl(StampSendRecordMapper stampSendRecordMapper, RoomMapper roomMapper,
                                ApplicationEventPublisher eventPublisher) {
        this.stampSendRecordMapper = stampSendRecordMapper;
        this.roomMapper = roomMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public StampSendRecord saveStamp(Integer userId, StampSendRequest stampSendRequest) {
        StampSendRecord record = new StampSendRecord();
        record.setUserId(userId);
        record.setStampId(stampSendRequest.getStampId());
        record.setRoomId(stampSendRequest.getRoomId());
        // createdAt は DB 側の default で入る想定

        // room の存在チェック、active チェック
        RoomEntity room = roomMapper.selectById(record.getRoomId());
        if (room == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "指定されたルームは存在しません (roomId=" + record.getRoomId() + ")");
        }
        if (Boolean.FALSE.equals(room.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "このルームは既に閉じています");
        }

        stampSendRecordMapper.insertStampSendRecord(record);

        // イベントを publish。リスナー側で AFTER_COMMIT を指定しているため
        // 実際の処理（pushSummary）はトランザクションコミット後に実行されます。
        try {
            long roomId = record.getRoomId(); // プリミティブを使ってボクシングを避ける
            eventPublisher.publishEvent(new StampLogCreatedEvent(roomId));
        } catch (Exception ex) {
            logger.warn("Failed to publish StampLogCreatedEvent: {}", ex.getMessage());
        }

        return record;
    }
}