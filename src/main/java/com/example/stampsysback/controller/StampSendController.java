package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.dto.StampSendResponse;
import com.example.stampsysback.entity.StampSendRecord;
import com.example.stampsysback.service.StampSendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stamp-send")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSendController {

    private final StampSendService stampSendService;

    public StampSendController(StampSendService stampSendService) {
        this.stampSendService = stampSendService;
    }

    @PostMapping
    public ResponseEntity<StampSendResponse> stampSend(@RequestBody StampSendRequest stampSendRequest){

        Integer userId = stampSendRequest.getUserId();
        StampSendRecord saved = stampSendService.saveStamp(userId, stampSendRequest);

        StampSendResponse stampSendResponse = new StampSendResponse();
        stampSendResponse.setSuccess(true);
        stampSendResponse.setMessage("次にスタンプを押せるまで30秒");
        return ResponseEntity.ok(stampSendResponse);
    }
}
