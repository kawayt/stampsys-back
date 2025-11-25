package com.example.stampsysback.service;

import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.form.RoomForm;


import java.util.List;

public interface RoomService {
    // クラスIDに紐づくルーム一覧を取得
    List<RoomEntity> selectByClassId(Integer classId);

    RoomDto insertRoom(RoomForm roomForm);

    Integer findClassIdByRoomId(Integer roomId);

    //指定 roomId の active を false にしてルームを終了する。
    void closeRoom(Integer roomId);

    //指定 roomId の hidden を true にしてルームを削除する。
    void deleteRoom(Integer roomId);
}
