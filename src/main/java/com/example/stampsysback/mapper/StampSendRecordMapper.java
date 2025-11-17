package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.StampSendRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StampSendRecordMapper {
    // public は省略可能（interface のメソッドは暗黙的に public）
    int insertStampSendRecord(StampSendRecord stampSendRecord);
}