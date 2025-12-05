package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampManagementRequest;
import com.example.stampsysback.dto.StampManagementResponse;
import com.example.stampsysback.mapper.StampManagementMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/stamp-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StampManagementController {

    private static final Logger logger = LoggerFactory.getLogger(StampManagementController.class);
    private final StampManagementMapper stampManagementMapper;

    // スタンプ一覧取得（全件）
    @GetMapping
    public List<StampManagementResponse> getAllStamps() {
        return stampManagementMapper.findAll();
    }

    // 自分が作成したスタンプ一覧取得
    @GetMapping("/mine")
    public List<StampManagementResponse> getMyStamps(@RequestParam(required = false) Integer userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId パラメータを指定してください");
        }
        return stampManagementMapper.selectByUserId(userId);
    }

    // 追加: 指定クラスに紐づくスタンプ（割当済み）
    @GetMapping("/class/{classId}/assigned")
    public List<StampManagementResponse> getAssignedStamps(@PathVariable Integer classId) {
        return stampManagementMapper.selectStampsByClassId(classId);
    }

    // 追加: 指定クラスに紐づいていないスタンプ（未割当）
    @GetMapping("/class/{classId}/unassigned")
    public List<StampManagementResponse> getUnassignedStamps(@PathVariable Integer classId) {
        return stampManagementMapper.selectStampsNotInClassId(classId);
    }

    // 新しいスタンプの追加
    @PostMapping
    public void addStamp(@RequestBody @Valid StampManagementRequest dto) {
        // 簡易チェックとログ（userId が null かどうかをログに出します）
        if (dto.getUserId() == null) {
            logger.warn("addStamp called with null userId. stamps.user_id will be inserted as NULL.");
        } else {
            logger.info("addStamp called by userId={}", dto.getUserId());
        }
        stampManagementMapper.insert(dto);
    }

    // スタンプ論理削除
    @DeleteMapping("/{stampId}")
    public void deleteStamp(@PathVariable int stampId) {
        stampManagementMapper.delete(stampId);
    }

    // 【追加】管理者限定: 論理削除済みスタンプ一覧取得
    @GetMapping("/deleted")
    public List<StampManagementResponse> getDeletedStamps() {
        logger.info("getDeletedStamps called (no admin check)");
        return stampManagementMapper.selectDeleted();
    }

    //【追加】スタンプ復元
    @PostMapping("/restore/{stampId}")
    public void restoreStamp(@PathVariable int stampId) { stampManagementMapper.restore(stampId); }

}
