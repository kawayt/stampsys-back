package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.event.StampSavedEvent;
import com.example.stampsysback.mapper.RoomMapper;
import com.example.stampsysback.mapper.StampSendRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

//ビジネスロジック(DTO(StampSendRequest,StampSendResponseから、
//Entity(StampSendRecord)を生成し、StampSendRecordMapperを呼び出す
@Service
public class StampSendServiceImpl implements StampSendService {

    private static final Logger logger = LoggerFactory.getLogger(StampSendServiceImpl.class);

    private final StampSendRecordMapper stampSendRecordMapper;
    private final RoomMapper roomMapper;
    private final ApplicationEventPublisher eventPublisher;

    public StampSendServiceImpl(StampSendRecordMapper stampSendRecordMapper,
                                RoomMapper roomMapper,
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
        // createdAt はデフォルトで現在時刻が入るようにしている

        // 追加: room が存在し、かつ active == true の場合のみスタンプ送信を許可する
        RoomEntity room = roomMapper.selectById(record.getRoomId());
        if (room == null) {
            // ルームが存在しない場合はエラー（HTTP ステータスは Controller 側で変えたいなら別途ハンドリングしてください）
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "指定されたルームは存在しません (roomId=" + record.getRoomId() + ")");
        }
        if (Boolean.FALSE.equals(room.getActive())) {
            // ルームが closed (active = false) の場合は登録しない
            throw new ResponseStatusException(HttpStatus.CONFLICT, "このルームは既に閉じています");
        }

        // 保存
        stampSendRecordMapper.insertStampSendRecord(record);

        // 保存成功後にイベントを発行（リスナー側で AFTER_COMMIT を使ってコミット完了後に通知する設計）
        try {
            // プリミティブ int -> long に暗黙変換され、StampSavedEvent のコンストラクタは long を受けるため
            eventPublisher.publishEvent(new StampSavedEvent(this, record.getRoomId()));
        } catch (Exception ex) {
            // 通知失敗で本処理を失敗させたくないのでログのみ
            logger.warn("Failed to publish StampSavedEvent for room {}", record.getRoomId(), ex);
        }

        return record;
    }
}