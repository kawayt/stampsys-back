package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.RoomEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMapper {
    // クラスIDに基づいてルームの情報を取得、RoomEntityのリストを返す
    List<RoomEntity> selectByClassId(@Param("classId") Integer classId);

    // ルームを作成
    int insert(RoomEntity roomEntity);

    // room_idで検索
    RoomEntity selectById(@Param("roomId") Integer roomId);

    // 現在の最大room_idを返す（存在しなければ０）
    Integer selectMaxRoomId(); // 最大ID取得

    Integer findClassIdByRoomId(@Param("roomId") Integer roomId);

    //指定 roomId の active を更新する
    int updateActiveById(@Param("roomId") Integer roomId, @Param("active") Boolean active);

}
