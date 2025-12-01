package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.StampSendLogDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StampSendLogQueryMapper {
    /**
     * 指定 userId と roomId に紐づく送信済みスタンプ履歴を sent_at 降順で取得する
     */
    List<StampSendLogDto> findByUserIdAndRoomId(@Param("userId") int userId, @Param("roomId") int roomId);
}