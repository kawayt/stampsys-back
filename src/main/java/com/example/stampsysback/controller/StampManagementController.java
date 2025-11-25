package com.example.stampsysback.controller;

import com.example.stampsysback.dto.StampManagementRequest;
import com.example.stampsysback.dto.StampManagementResponse;
import com.example.stampsysback.mapper.StampManagementMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stamp-management")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StampManagementController {

    private final StampManagementMapper stampManagementMapper;

    // スタンプ一覧取得（全件）
    @GetMapping
    public List<StampManagementResponse> getAllStamps() {
        return stampManagementMapper.findAll();
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
        stampManagementMapper.insert(dto);
    }

    // スタンプ論理削除
    @DeleteMapping("/{stampId}")
    public void deleteStamp(@PathVariable int stampId) {
        stampManagementMapper.delete(stampId);
    }
}
