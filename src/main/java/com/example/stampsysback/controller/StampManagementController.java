package com.example.stampsysback.controller;

import com.example.stampsysback.entity.StampManagementEntity;
import com.example.stampsysback.mapper.StampManagementMapper;
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
    public List<StampManagementEntity> getAllStamps() {
        return stampManagementMapper.findAll();
    }

    // 新しいスタンプの追加
    @PostMapping
    public void addStamp(@RequestBody StampManagementEntity stamp) {
        stampManagementMapper.insert(stamp);
    }

    // スタンプの削除
    @DeleteMapping("/{stampId}")
    public void deleteStamp(@PathVariable int stampId) {
        stampManagementMapper.delete(stampId);
    }
}

