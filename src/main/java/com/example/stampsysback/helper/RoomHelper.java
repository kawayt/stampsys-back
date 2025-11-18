package com.example.stampsysback.helper;

import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.form.RoomForm;
import lombok.Data;

import java.time.OffsetDateTime;

public class RoomHelper {
    // RoomForm → RoomEntity に変換
    public static RoomEntity toEntity(RoomForm roomForm){
        RoomEntity roomEntity = new RoomEntity();
        roomEntity.setRoomName(roomForm.getRoomName());
        roomEntity.setClassId(roomForm.getClassId());
        //Activeがnullじゃない場合、その値をセット。nullの場合はBoolean.TRUEをセット
        roomEntity.setActive(roomForm.getActive() != null? roomForm.getActive(): Boolean.TRUE);
        roomEntity.setCreatedAt(OffsetDateTime.now());
        return roomEntity;
    }

    // RoomEntity → DTOに変換
    public static RoomDto toDto(RoomEntity roomEntity){
        // Entityがnullの場合（上のtoEntityメソッドを行っていない場合）、nullを返す
        if(roomEntity == null) return null;
        RoomDto roomDto = new RoomDto();
        roomDto.setRoomId(roomEntity.getRoomId());
        roomDto.setRoomName(roomEntity.getRoomName());
        roomDto.setClassId(roomEntity.getClassId());
        roomDto.setActive(roomEntity.getActive());
        roomDto.setCreatedAt(roomEntity.getCreatedAt());
        return roomDto;
    }

}
