package com.example.stampsysback.redis;

import com.example.stampsysback.config.RedisConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis に roomId を publish するユーティリティ。
 */
@Component
public class StampSummaryRedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public StampSummaryRedisPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String originInstanceId, long roomId) {
        try {
            StampSummaryRedisMessage msg = new StampSummaryRedisMessage(originInstanceId, roomId);
            String payload = objectMapper.writeValueAsString(msg);
            redisTemplate.convertAndSend(RedisConfig.CHANNEL, payload);
        } catch (JsonProcessingException ex) {
            // 運用時はログ出力を追加してください
        }
    }
}