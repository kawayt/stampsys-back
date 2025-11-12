package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.dto.StampSendResponse;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.service.StampSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//POSTリクエストを受け取り、StampSendServiceを呼び出すコントローラー
@RestController
@RequestMapping("/api/stamp-send")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSendController {

    private static final Logger logger = LoggerFactory.getLogger(StampSendController.class);

    private final StampSendService stampSendService;

    public StampSendController(StampSendService stampSendService) {
        this.stampSendService = stampSendService;
    }

    @PostMapping
    public ResponseEntity<StampSendResponse> stampSend(@RequestBody StampSendRequest stampSendRequest){

        Integer userId = stampSendRequest.getUserId();
        StampSendRecord stampSendRecord = stampSendService.saveStamp(stampSendRequest.getUserId(), stampSendRequest);

        if (stampSendRecord != null) {
            logger.info("saved stamp: stampLogId={}, userId={}, roomId={}, stampId={}",
                    stampSendRecord.getStampLogId(),
                    stampSendRecord.getUserId(),
                    stampSendRecord.getRoomId(),
                    stampSendRecord.getStampId());
        } else {
            logger.warn("stampSave returned null for userId={}", stampSendRequest.getUserId());
        }

        StampSendResponse stampSendResponse = new StampSendResponse();
        stampSendResponse.setSuccess(true);

        return ResponseEntity.ok(stampSendResponse);
    }
}
