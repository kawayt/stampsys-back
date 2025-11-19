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
     * ルームに紐づくスタンプ一覧を返す。
     * GET /api/rooms/{roomId}/stamps
     * - 認証済みであることを前提とし、ルームが属する class_id に
     *   対してユーザーが所属している（users_classes に存在）か、
     *   またはユーザーが TEACHER/ADMIN の場合にのみスタンプ一覧を返します。
     * - それ以外は 403 とエラーメッセージを返します。
     */
    @GetMapping("/rooms/{roomId}/stamps")
    public ResponseEntity<?> getStampsByRoom(
            @PathVariable Integer roomId,
            @AuthenticationPrincipal OAuth2User principal) {

        // require authentication
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "ログインが必要です"));
        }

        // resolve current user's DB id (userId) via AuthorizationService helper
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

        // authorized -> return stamps
        List<StampDto> stamps = stampDisplayService.findStampsByRoomId(roomId);
        return ResponseEntity.ok(stamps);
    }
}