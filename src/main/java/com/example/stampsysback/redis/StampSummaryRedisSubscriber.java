package com.example.stampsysback.redis;

import com.example.stampsysback.sse.StampSummaryEmitterRegistry;
import com.example.stampsysback.service.StampSummaryService;
import com.example.stampsysback.util.InstanceIdProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Redis からのメッセージを受け取って、該当ルームの集計を取得し broadcast する。
 * originInstanceId が自分自身と同じ場合は無視する。
 */
@Component
public class StampSummaryRedisSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(StampSummaryRedisSubscriber.class);

    private final StampSummaryService summaryService;
    private final StampSummaryEmitterRegistry emitterRegistry;
    private final InstanceIdProvider instanceIdProvider;
    private final ObjectMapper objectMapper;

    public StampSummaryRedisSubscriber(StampSummaryService summaryService,
                                       StampSummaryEmitterRegistry emitterRegistry,
                                       InstanceIdProvider instanceIdProvider,
                                       ObjectMapper objectMapper) {
        this.summaryService = summaryService;
        this.emitterRegistry = emitterRegistry;
        this.instanceIdProvider = instanceIdProvider;
        this.objectMapper = objectMapper;
    }

    // MessageListenerAdapter が呼ぶメソッド名（RedisConfig に合わせる）
    public void onMessage(String message) {
        try {
            StampSummaryRedisMessage m = objectMapper.readValue(message, StampSummaryRedisMessage.class);
            if (m == null) return;
            if (instanceIdProvider.getInstanceId().equals(m.getOriginInstanceId())) {
                // 自分が発行したメッセージは無視
                logger.debug("Ignoring Redis message from same instance: {}", m.getOriginInstanceId());
                return;
            }
            long roomId = m.getRoomId();
            logger.info("Received Redis notification for roomId={} from origin={}", roomId, m.getOriginInstanceId());

            var summary = summaryService.getStampDistributionForRoom(roomId);
            emitterRegistry.broadcast(roomId, summary);
        } catch (Exception ex) {
            logger.warn("Failed to handle Redis message: {}", message, ex);
        }
    }
}