package com.example.stampsysback.service;

import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.form.RoomForm;
import com.example.stampsysback.helper.RoomHelper;
import com.example.stampsysback.mapper.ClassMapper;
import com.example.stampsysback.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{
    // DI
    private final RoomMapper roomMapper;
    private final ClassMapper classMapper;
    private static final int MAX_RETRIES = 5;
    private static final long BASE_BACKOFF_MS = 50L; // 基本待ち時間（ミリ秒）

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Override
    public Integer findClassIdByRoomId(Integer roomId) {
        return roomMapper.findClassIdByRoomId(roomId);
    }

    @Override
    public List<RoomEntity> selectByClassId(Integer classId){
        return roomMapper.selectByClassId(classId);
    }

    @Override
    public RoomDto insertRoom(RoomForm roomForm){

        // class_id存在チェック
        Integer exists = classMapper.existsById(roomForm.getClassId());

        if (exists == null) {
            throw new IllegalArgumentException("指定された classId が存在しません: " + roomForm.getClassId());
        }

        // 同クラスでactive = trueのルームをすべてfalseにする
        try{
            int deactivated = roomMapper.updateDeactivateByClassId(roomForm.getClassId());
            logger.debug("updateDeactivateByClassId affected rows: {}", deactivated);
        } catch (DataAccessException ex){
            logger.error("Failed to deactivate existing active rooms for classId={}", roomForm.getClassId(), ex);
            throw ex;
        }

        // RoomFormをRoomEntityに変換
        RoomEntity toInsert = RoomHelper.toEntity(roomForm);

        // 最大試行回数でループ（while(true) を避ける）
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            // 現在の最大room_idを取得して candidate を決定
            Integer maxId = roomMapper.selectMaxRoomId();
            int candidateId = (maxId != null ? maxId : 0) + 1;
            toInsert.setRoomId(candidateId);

            try {
                int rows = roomMapper.insert(toInsert);
                if (rows == 1) {
                    RoomEntity created = roomMapper.selectById(toInsert.getRoomId());
                    return RoomHelper.toDto(created);
                } else {
                    // 想定外: 影響行数が 1 でない
                    throw new DataAccessException("Room insert failed, affected rows: " + rows) {};
                }
            } catch (DuplicateKeyException dkEx) {
                // 競合が発生した場合はリトライ（最後の試行なら例外を投げる）
                if (attempt == MAX_RETRIES) {
                    throw new DataAccessException("Failed to insert room after " + MAX_RETRIES + " retries due to duplicate key", dkEx) {};
                }
                // 指数バックオフ + ランダムジッタで待機（ビジーウェイトではない）
                long backoff = computeBackoffWithJitter(attempt);
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DataAccessException("Insert interrupted while retrying", ie) {};
                }
                // 次のループで再試行
            }
        }

        // 理論上ここには到達しないが安全のため例外を投げる
        throw new DataAccessException("Failed to insert room (unexpected control flow)") {};
    }

    //指定したroomIdをfalseに変更する
    @Override
    public void closeRoom(Integer roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId が指定されていません");
        }
        logger.debug("closeRoom called for roomId={}", roomId);

        // 存在チェック
        RoomEntity existing = roomMapper.selectById(roomId);
        logger.debug("existing room (selectById) result: {}", existing);

        if (existing == null) {
            throw new IllegalArgumentException("指定された room が見つかりません: " + roomId);
        }
        try {
            int rows = roomMapper.updateActiveById(roomId, Boolean.FALSE);
            logger.debug("updateActiveById affected rows: {}", rows);
            if (rows == 0) {
                throw new DataAccessException("active 更新が行われませんでした for roomId=" + roomId) {};
            }
        } catch (DataAccessException ex) {
            logger.error("Failed to close room {}", roomId, ex);
            throw ex;
        }
    }

    //指定したroomIdをfalseに変更する
    @Override
    public void deleteRoom(Integer roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId が指定されていません");
        }
        logger.debug("deleteRoom called for roomId={}", roomId);

        // 存在チェック
        RoomEntity existing = roomMapper.selectById(roomId);
        logger.debug("existing room (selectById) result: {}", existing);

        if (existing == null) {
            throw new IllegalArgumentException("指定された room が見つかりません: " + roomId);
        }
        try {
            int rows = roomMapper.updateHiddenById(roomId, Boolean.TRUE);
            logger.debug("updateHiddenById affected rows: {}", rows);
            if (rows == 0) {
                throw new DataAccessException("hidden 更新が行われませんでした for roomId=" + roomId) {};
            }
        } catch (DataAccessException ex) {
            logger.error("Failed to delete room {}", roomId, ex);
            throw ex;
        }
    }

    /**
     * 指定したClassIdに紐づく、activeな最新のルームを返す。
     */
    @Override
    public Integer findLatestActiveRoomIdByClassId(Integer classId) {
        if (classId == null) {
            throw new IllegalArgumentException("classId が指定されていません");
        }
        // Mapper が存在しなければ null を返す設計（呼び出し側で 404 にする）
        return roomMapper.selectLatestActiveRoomIdByClassId(classId);
    }

    /**
     * 指数バックオフ（base * 2^(attempt-1)）にランダムジッタを加えた待機時間（ミリ秒）を返す。
     * attempt は 1 から始まる試行回数。
     */
    private long computeBackoffWithJitter(int attempt) {
        long exp = BASE_BACKOFF_MS * (1L << (Math.max(0, attempt - 1)));
        // 上限を設定してオーバーフローを防ぐ（例えば 2s）
        long capped = Math.min(exp, 2000L);
        // ジッタ: 0..(capped/2)
        long jitter = ThreadLocalRandom.current().nextLong(capped / 2 + 1);
        return capped / 2 + jitter;
    }
}