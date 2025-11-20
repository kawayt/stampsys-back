package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.ClassEntity;
import com.example.stampsysback.entity.RoomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// users_classes 中間テーブル用Mapperと、ユーザーに見えるクラス・ルーム取得
@Mapper
public interface UserClassMapper {
    // users_classesに追加(uer_id,class_idのペア）
    // return 影響行数
    int insertUserClass(@Param("userId") Integer userId, @Param("classId") Integer classId);

    // users_classesから削除
    int deleteUserClass(@Param("userId") Integer userId, @Param("classId") Integer classId);

    // 指定クラスに追加ユーザーがすでに追加されているかチェック、重複を防ぐため
    Integer existsRelation(@Param("userId") Integer userId, @Param("classId") Integer classId);

    // 追加ユーザーが見えるクラス一覧を返す
    List<ClassEntity> selectClassByUserId(@Param("userId") Integer userId);

    // 追加ユーザが見えるルーム一覧を返す（そのユーザーが追加されているクラスに紐づくroom）
    List<RoomEntity> selectRoomByUserId(@Param("userId") Integer userId);

}
