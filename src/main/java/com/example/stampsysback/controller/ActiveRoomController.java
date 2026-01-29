package com.example.stampsysback.controller;

import com.example.stampsysback.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ActiveRoomController {

    private static final Logger logger = LoggerFactory.getLogger(ActiveRoomController.class);

    private final RoomService roomService;

    /**
     * 指定クラスの最新の active = true なルームの roomId を返す。
     * - 成功: 200 { "roomId": 123 }
     * - 存在しない: 404 とメッセージ
     * - 不正な入力: 400
     */
    @GetMapping("/{classId}/active-room")
    public ResponseEntity<?> getLatestActiveRoom(@PathVariable Integer classId) {
        try {
            Integer roomId = roomService.findLatestActiveRoomIdByClassId(classId);
            if (roomId == null) {
                return ResponseEntity.status(404)
                        .body("現在ルームが開いていません。教員がルームを開くのを待ってください。");
            }
            return ResponseEntity.ok(Map.of("roomId", roomId));
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid request for classId={}", classId, ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Failed to get latest active room for classId={}", classId, ex);
            return ResponseEntity.internalServerError().body("サーバエラーが発生しました");
        }
    }
}