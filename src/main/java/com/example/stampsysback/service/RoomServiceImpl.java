package com.example.stampsysback.service;

import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.form.RoomForm;
import com.example.stampsysback.helper.RoomHelper;
import com.example.stampsysback.mapper.ClassMapper;
import com.example.stampsysback.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{
    // DI
    private final RoomMapper roomMapper;
    private final ClassMapper classMapper;
    private static final int MAX_RETRIES = 5;

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
        // フォームから受けとったクラスIDがexitsByIdでマッパー　→　XMLであるか確認
        Integer exists = classMapper.existsById(roomForm.getClassId());

        if (exists == null) {
            throw new IllegalArgumentException("指定された classId が存在しません: " + roomForm.getClassId());
        }
          // 保留
        // 同一 class 内で room_name の重複チェック
        //Room dup = roomMapper.selectByNameAndClass(form.getRoomName(), form.getClassId());
        //if (dup != null) {
        //throw new IllegalStateException("同じクラス内に同名の部屋が既に存在します: " + form.getRoomName());
        //}

        // RoomFormをRoomEntityに変換したものをtoInsertへ
        RoomEntity toInsert = RoomHelper.toEntity(roomForm);


        // 採番: MAX(room_id) + 1 を candidate として insert を試み、重複が起きたら再試行する

        // リトライ回数をカウントする変数、最初は0
        int attempt = 0;
        // 最大MAX_RETRIES（五回）まで繰り返す。
        while (true) {
            // リトライ回数を足す
            attempt++;

            // 一番新しいroom_idを取得
            Integer maxId = roomMapper.selectMaxRoomId();

            // 最大値があればmaxId+1、なければ0+1を新しいIDとする
            int candidateId = (maxId != null ? maxId : 0) + 1;
            // 新しく追加するroom_idをエンティティ → DTOへ変換してtoInsertにセット
            toInsert.setRoomId(candidateId);

            // DBへ挿入を試みる
            try {
                // 新しく追加するroom_idをセットしてroomを作る
                int rows = roomMapper.insert(toInsert);
                if (rows == 1) {
                    // 成功!!!

                    // room_idで検索してselectByIdでroom_idに対応したroomを取得
                    RoomEntity created = roomMapper.selectById(toInsert.getRoomId());
                    // 取得したroomをEntity → DTOに変換して返す
                    return RoomHelper.toDto(created);
                } else {
                    // 想定外: 影響行数が 1 でない場合は失敗として例外
                    throw new DataAccessException("Room insert failed, affected rows: " + rows) {};
                }
            } catch (DuplicateKeyException dkEx) {
                // 同時挿入で新しく追加されるroom_idが競合した場合はリトライ
                if (attempt >= MAX_RETRIES) {
                    throw new DataAccessException("Failed to insert room after retries due to duplicate key", dkEx) {};
                }
                // 少し待って再試行することで競合が解消される可能性を上げる
                try { Thread.sleep(50L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
