package com.example.stampsysback.listener;

import com.example.stampsysback.event.StampSavedEvent;
import com.example.stampsysback.sse.StampSummaryEmitterRegistry;
import com.example.stampsysback.service.StampSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * StampSavedEvent を受け取り、トランザクションコミット後（AFTER_COMMIT）に
 * 最新の集計を取得して接続中クライアントへ通知するリスナー。
 */
@Component
public class StampSavedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(StampSavedEventListener.class);

    private final StampSummaryService summaryService;
    private final StampSummaryEmitterRegistry emitterRegistry;

    public StampSavedEventListener(StampSummaryService summaryService,
                                   StampSummaryEmitterRegistry emitterRegistry) {
        this.summaryService = summaryService;
        this.emitterRegistry = emitterRegistry;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(StampSavedEvent event) {
        long roomId = event.getRoomId();
        try {
            var summary = summaryService.getStampDistributionForRoom(roomId);
            emitterRegistry.broadcast(roomId, summary);
        } catch (Exception ex) {
            logger.warn("Failed to broadcast stamp summary for room {}", roomId, ex);
        }
    }
}