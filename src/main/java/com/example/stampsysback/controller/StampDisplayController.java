package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampDto;
import com.example.stampsysback.service.RoomService;
import com.example.stampsysback.service.StampDisplayService;
import com.example.stampsysback.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class StampDisplayController {

    private final StampDisplayService stampDisplayService;
    private final RoomService roomService;
    private final AuthorizationService authorizationService;

    public StampDisplayController(StampDisplayService stampDisplayService,
                                  RoomService roomService,
                                  AuthorizationService authorizationService){
        this.stampDisplayService = stampDisplayService;
        this.roomService = roomService;
        this.authorizationService = authorizationService;
    }

    /**
     * ルームに紐づくスタンプ一覧を返す。加えて room_name を含める。
     * GET /api/rooms/{roomId}/stamps
     */
    @GetMapping("/rooms/{roomId}/stamps")
    public ResponseEntity<?> getStampsByRoom(
            @PathVariable Integer roomId,
            @AuthenticationPrincipal OAuth2User principal) {

        // require authentication
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "ログインが必要です"));
        }

        // resolve current user's DB id (userId)
        Integer userId = authorizationService.resolveCurrentUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "ユーザー情報が見つかりません"));
        }

        // find classId for the room
        Integer classId = roomService.findClassIdByRoomId(roomId);
        if (classId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "指定されたルームが見つかりません"));
        }

        // authorization: must be member of class OR teacher/admin
        boolean allowed = authorizationService.isUserInClass(userId, classId) || authorizationService.isTeacherOrAdmin(userId);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "ルームの参加資格がありません"));
        }

        // authorized -> fetch room name and stamps
        String roomName = roomService.findRoomNameByRoomId(roomId); // 追加: RoomService に実装
        if (roomName == null) {
            // room が見つからない（安全策）
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "指定されたルームが見つかりません"));
        }

        List<StampDto> stamps = stampDisplayService.findStampsByRoomId(roomId);

        // 返却フォーマット: { roomId, roomName, stamps: [...] }
        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "roomName", roomName,
                "stamps", stamps
        ));
    }
}