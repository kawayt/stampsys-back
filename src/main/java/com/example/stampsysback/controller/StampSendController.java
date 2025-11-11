package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.dto.StampSendResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stamp-send")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSendController {

    @PostMapping("stamp-send")
    public ResponseEntity<StampSendResponse> stampSend(@RequestBody StampSendRequest stampSendRequest){
        StampSendResponse stampSendResponse = new StampSendResponse();
        stampSendResponse.setStatus("OK");
        stampSendResponse.setMessage("スタンプを送信しました。");
        return ResponseEntity.ok(stampSendResponse);
    }
}
