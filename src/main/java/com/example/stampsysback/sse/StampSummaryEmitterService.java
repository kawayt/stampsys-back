package com.example.stampsysback.sse;

import com.example.stampsysback.dto.StampSummary;
import com.example.stampsysback.service.StampSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * インスタンス固有の UUID を含む instanceId をログに出す実装（複数 JVM でも判別可能）
 */
@Service
public class StampSummaryEmitterService {

    private static final Logger logger = LoggerFactory.getLogger(StampSummaryEmitterService.class);

    // roomId -> list of emitters
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final StampSummaryService stampSummaryService;
    private final String instanceId;

    public StampSummaryEmitterService(StampSummaryService stampSummaryService) {
        this.stampSummaryService = stampSummaryService;
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown-host";
        }
        // ホスト名だけだと同一ホスト上の複数 JVM を区別できないので UUID を付与する
        this.instanceId = host + "-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("StampSummaryEmitterService initialized on instanceId={}", this.instanceId);
    }

    public SseEmitter createEmitter(Long roomId, long timeoutMillis) {
        final SseEmitter emitter = new SseEmitter(timeoutMillis);
        emitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        try {
            List<StampSummary> data = stampSummaryService.getStampDistributionForRoom(roomId);
            emitter.send(SseEmitter.event().name("summary").data(data, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            // ignore
        }

        emitter.onCompletion(() -> removeEmitter(roomId, emitter));
        emitter.onTimeout(() -> removeEmitter(roomId, emitter));
        emitter.onError((ex) -> removeEmitter(roomId, emitter));

        logger.debug("Created emitter for roomId={} on instanceId={}, totalEmittersForRoom={}",
                roomId, instanceId, emitters.getOrDefault(roomId, Collections.emptyList()).size());
        return emitter;
    }

    private void removeEmitter(Long roomId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(roomId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(roomId);
            }
        }
        try { emitter.complete(); } catch (Exception ignored) {}
        logger.debug("Removed emitter for roomId={} on instanceId={}, remaining={}", roomId, instanceId,
                emitters.getOrDefault(roomId, Collections.emptyList()).size());
    }

    public void pushSummary(Long roomId) {
        logger.info("pushSummary called for roomId={} on instanceId={}", roomId, instanceId);
        List<SseEmitter> list = emitters.get(roomId);
        if (list == null || list.isEmpty()) {
            logger.info("No connected emitters for roomId={} on instanceId={}", roomId, instanceId);
            return;
        }

        List<StampSummary> data = stampSummaryService.getStampDistributionForRoom(roomId);
        for (SseEmitter emitter : new ArrayList<>(list)) {
            try {
                emitter.send(SseEmitter.event().name("summary").data(data, MediaType.APPLICATION_JSON));
            } catch (Exception ex) {
                removeEmitter(roomId, emitter);
            }
        }
        logger.info("Pushed summary to {} emitters for roomId={} on instanceId={}", list.size(), roomId, instanceId);
    }
}