package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.StampDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StampDisplayMapper {
    List<StampDto> findStampsByRoomId(@Param("roomId") Integer roomId);
    List<StampDto> findStampsByClassId(@Param("classId") Integer classId);
}
