package com.example.stampsysback.listener;

import com.example.stampsysback.event.StampSavedEvent;
import com.example.stampsysback.redis.StampSummaryRedisPublisher;
import com.example.stampsysback.sse.StampSummaryEmitterRegistry;
import com.example.stampsysback.service.StampSummaryService;
import com.example.stampsysback.util.InstanceIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * StampSavedEvent を受け取り、トランザクションコミット後（AFTER_COMMIT）に
 * - 同一インスタンスの接続に対しては直接 broadcast、
 * - 他インスタンスへは Redis を通じて通知する（各インスタンスが受け取って broadcast）
 */
@Component
public class StampSavedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(StampSavedEventListener.class);

    private final StampSummaryService summaryService;
    private final StampSummaryEmitterRegistry emitterRegistry;
    private final StampSummaryRedisPublisher redisPublisher;
    private final InstanceIdProvider instanceIdProvider;

    public StampSavedEventListener(StampSummaryService summaryService,
                                   StampSummaryEmitterRegistry emitterRegistry,
                                   StampSummaryRedisPublisher redisPublisher,
                                   InstanceIdProvider instanceIdProvider) {
        this.summaryService = summaryService;
        this.emitterRegistry = emitterRegistry;
        this.redisPublisher = redisPublisher;
        this.instanceIdProvider = instanceIdProvider;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(StampSavedEvent event) {
        long roomId = event.getRoomId();
        try {
            // 1) ローカル接続へ直接送信
            var summary = summaryService.getStampDistributionForRoom(roomId);
            emitterRegistry.broadcast(roomId, summary);

            // 2) 他インスタンスへ通知（origin に自分の instanceId を付与）
            redisPublisher.publish(instanceIdProvider.getInstanceId(), roomId);
        } catch (Exception ex) {
            logger.warn("Failed to broadcast stamp summary for room {}", roomId, ex);
        }
    }
}