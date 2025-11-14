package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.RoomEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomMapper {
    // クラスIDに基づいてルームの情報を取得、RoomEntityのリストを返す
    List<RoomEntity> selectBYClassId(Integer classId);
}
