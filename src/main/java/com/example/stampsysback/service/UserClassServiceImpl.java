package com.example.stampsysback.service;


import com.example.stampsysback.dto.ClassDto;
import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.ClassEntity;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.helper.RoomHelper;
import com.example.stampsysback.mapper.ClassMapper;
import com.example.stampsysback.mapper.UserClassMapper;
import com.example.stampsysback.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserClassServiceImpl implements UserClassService{
    private final UserClassMapper userClassMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;

    @Override
    public int addUserToClass(Integer userId, Integer classId){
        // ユーザー存在チェック
        Integer userExists = userMapper.existsById(userId);
        // いたら下へ、いなかったら（null）だったら例外
        if (userExists == null){
            throw new IllegalArgumentException("指定された userId が存在しません:" + userId);
        }

        // クラス存在チェック
        Integer classExists = classMapper.existsById(classId);
        if(classExists == null){
            throw new IllegalArgumentException("指定された classId が存在しません:" + classId);
        }

        // 指定クラスに追加ユーザーがすでに追加されているかチェック
        // 既に追加されていたら何もしない（0を返す）
        Integer rel = userClassMapper.existsRelation(userId, classId);
        // rel == 1だったら既に追加されているということ。何もしないで rel = 0 を返す
        // まだ追加されていなかったら下へ行ってinsertを実行
        if(rel != null){
            return 0;
        }

        // users_classesを追加。insertを実行して影響行数を返す（通常1）
        int rows = userClassMapper.insertUserClass(userId, classId);
        if(rows == 0){
            throw new DataAccessException("ユーザーをクラスに追加できませんでした"){};
        }
        return rows;
    }

    @Override
    public int removeUserFromClass(Integer userId, Integer classId){
        // users_classesから削除。影響行数を返す（1 = 削除した、0 = もともと存在しなかった）
        int rows = userClassMapper.deleteUserClass(userId, classId);
        return rows;
    }

    @Override
    // 指定ユーザーが指定クラスに所属しているか
    public boolean isUserInClass(Integer userId, Integer classId){
        Integer rel = userClassMapper.existsRelation(userId, classId);
        // 存在すれば何らかの値、存在しなければnullを返す
        return rel != null;
    }

    // 指定ユーザーが見えるクラス一覧
    @Override
    // ClassDtoに変換して返す
    public List<ClassDto> getClassForUser(Integer userId) {
        // userClassMapperを使って、指定されたuserIdに紐づくクラス一覧を取得
        List<ClassEntity> classEntity = userClassMapper.selectClassByUserId(userId);
        // 取得したclassEntityリストをStream API（ClassDto）に変換
        return classEntity.stream()
                // ClassEntityオブジェクトcをClassDtoに変換処理
                .map(c -> new ClassDto(c.getClassId(), c.getClassName(), c.getCreatedAt()))
                // Stream（変換後のClassDtoオブジェクト）をListにまとめて返す
                .collect(Collectors.toList());
    }

    // 指定ユーザーが見えるルーム一覧
    @Override
    public List<RoomDto> getRoomForUser(Integer userId) {
        // userClassMapperを使って、指定されたuserIdに紐づくルーム一覧を取得
        List<RoomEntity> roomEntity = userClassMapper.selectRoomByUserId(userId);
        // roomsをStreamに変換
        return roomEntity.stream()
                // RoomEntityオブジェクトをRoomDtoに変換処理(RoomHelperがもともとあるからそれ使う）
                .map(RoomHelper::toDto)
                // Stream（変換後のRoomDtoオブジェクト）をListにまとめて返す
                .collect(Collectors.toList());
    }
}
