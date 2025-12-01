package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampSendLogDto;
import com.example.stampsysback.mapper.StampSendLogQueryMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class StampSendLogController {

    private final StampSendLogQueryMapper stampSendLogQueryMapper;

    public StampSendLogController(StampSendLogQueryMapper stampSendLogQueryMapper) {
        this.stampSendLogQueryMapper = stampSendLogQueryMapper;
    }

    /**
     * 指定ユーザーが指定ルームで送信したスタンプ履歴を取得する
     * GET /api/rooms/{roomId}/users/{userId}/stamp-logs
     */
    @GetMapping("/rooms/{roomId}/users/{userId}/stamp-logs")
    public List<StampSendLogDto> getUserStampSendLogs(
            @PathVariable("roomId") int roomId,
            @PathVariable("userId") int userId) {

        return stampSendLogQueryMapper.findByUserIdAndRoomId(userId, roomId);
    }
}