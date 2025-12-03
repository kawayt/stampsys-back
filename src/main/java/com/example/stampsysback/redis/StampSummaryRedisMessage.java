package com.example.stampsysback.redis;

import lombok.Getter;
import lombok.Setter;

/**
 * Redis pub/sub でやり取りするメッセージ。
 */
@Getter
@Setter
public class StampSummaryRedisMessage {
    private String originInstanceId;
    private long roomId;

    public StampSummaryRedisMessage(String originInstanceId, long roomId) {
        this.originInstanceId = originInstanceId;
        this.roomId = roomId;
    }
}