package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampDto;
import com.example.stampsysback.service.StampDisplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StampDisplayController {

    private final StampDisplayService stampDisplayService;

    public StampDisplayController(StampDisplayService stampDisplayService){
        this.stampDisplayService = stampDisplayService;
    }

    /**
     * ルームに紐づくスタンプ一覧を返す。
     * GET /api/rooms/{roomId}/stamps
     */
    @GetMapping("/rooms/{roomId}/stamps")
    public ResponseEntity<List<StampDto>> getStampsByRoom(@PathVariable Integer roomId) {
        //stampsに、StampDisplayService.javaで返したstamp_id,stamp_name,stamp_color,stamp_iconを代入する
        List<StampDto> stamps = stampDisplayService.findStampsByRoomId(roomId);
        return ResponseEntity.ok(stamps);
    }

    //必要ならクラスIDで直接取得するエンドポイントも提供

    @GetMapping("/classes/{classId}/stamps")
    public ResponseEntity<List<StampDto>> getStampsByClass(@PathVariable Integer classId) {
        List<StampDto> stamps = stampDisplayService.findStampsByClassId(classId);
        return ResponseEntity.ok(stamps);
    }
}
