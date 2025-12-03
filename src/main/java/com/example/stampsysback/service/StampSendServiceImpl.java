package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.mapper.RoomMapper;
import com.example.stampsysback.mapper.StampSendRecordMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

//ビジネスロジック(DTO(StampSendRequest,StampSendResponseから、
//Entity(StampSendRecord)を生成し、StampSendRecordMapperを呼び出す
@Service
public class StampSendServiceImpl implements StampSendService {

    private final StampSendRecordMapper stampSendRecordMapper;
    private final RoomMapper roomMapper;

    public StampSendServiceImpl(StampSendRecordMapper stampSendRecordMapper, RoomMapper roomMapper) {
        this.stampSendRecordMapper = stampSendRecordMapper;
        this.roomMapper = roomMapper;
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

        stampSendRecordMapper.insertStampSendRecord(record);
        return record;
    }
}
