package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.StampSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StampSummaryMapper {
    /**
     * 指定ルームについて、
     * 参加者（rooms.class_id と同じ class_id を持つ users_classes の user_id）
     * を基準に「各ユーザーの最新押下（未押下は NULL）」を集計して、
     * スタンプ種類ごとの件数(cnt)と割合(pct)を返す。
     */
    List<StampSummary> findLatestStampDistributionByRoomId(@Param("roomId") Long roomId);
}