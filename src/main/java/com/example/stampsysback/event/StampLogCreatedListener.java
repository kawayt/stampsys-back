package com.example.stampsysback.event;

import com.example.stampsysback.sse.StampSummaryEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * StampLogCreatedEvent を受け取り、トランザクションコミット後に集計を push するリスナー。
 * TransactionalEventListener の phase = AFTER_COMMIT にしているので、イベントを publish した
 * メソッドがトランザクション内であってもコミット後に呼ばれます。
 */
@Component
public class StampLogCreatedListener {

    private static final Logger logger = LoggerFactory.getLogger(StampLogCreatedListener.class);

    private final StampSummaryEmitterService emitterService;

    public StampLogCreatedListener(StampSummaryEmitterService emitterService) {
        this.emitterService = emitterService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStampLogCreated(StampLogCreatedEvent event) {
        Long roomId = event.getRoomId();
        logger.info("Received StampLogCreatedEvent for roomId={}", roomId);
        try {
            emitterService.pushSummary(roomId);
        } catch (Exception ex) {
            logger.warn("Failed to push summary for roomId={} in listener: {}", roomId, ex.getMessage());
        }
    }
}
