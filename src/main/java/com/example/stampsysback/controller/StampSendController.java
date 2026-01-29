package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.dto.StampSendResponse;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.service.StampSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

//POSTリクエストを受け取り、StampSendServiceを呼び出すコントローラー
@RestController
@RequestMapping("/api/stamp-send")
public class StampSendController {

    private static final Logger logger = LoggerFactory.getLogger(StampSendController.class);

    private final StampSendService stampSendService;

    public StampSendController(StampSendService stampSendService) {
        this.stampSendService = stampSendService;
    }

    @PostMapping
    public ResponseEntity<StampSendResponse> stampSend(@RequestBody StampSendRequest stampSendRequest){

        try {
            StampSendRecord stampSendRecord = stampSendService.saveStamp(
                    stampSendRequest.getUserId(),
                    stampSendRequest
            );

            if (stampSendRecord != null) {
                logger.info("saved stamp: stampLogId={}, userId={}, roomId={}, stampId={}",
                        stampSendRecord.getStampLogId(),
                        stampSendRecord.getUserId(),
                        stampSendRecord.getRoomId(),
                        stampSendRecord.getStampId());

                StampSendResponse stampSendResponse = new StampSendResponse();
                stampSendResponse.setSuccess(true);
                return ResponseEntity.ok(stampSendResponse);
            } else {
                logger.warn("stampSave returned null for userId={}", stampSendRequest.getUserId());

                StampSendResponse stampSendResponse = new StampSendResponse();
                stampSendResponse.setSuccess(false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(stampSendResponse);
            }
        } catch (ResponseStatusException rse) {
            // Service 層で投げた ResponseStatusException は再スローしてグローバルハンドラに任せる
            throw rse;
        } catch (Exception e) {
            logger.error("Failed to save stamp for userId={}: {}",
                    stampSendRequest.getUserId(),
                    e.getMessage(),
                    e);

            StampSendResponse stampSendResponse = new StampSendResponse();
            stampSendResponse.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(stampSendResponse);
        }
    }
}