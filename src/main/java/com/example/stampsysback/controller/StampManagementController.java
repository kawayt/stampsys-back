package com.example.stampsysback.controller;

import com.example.stampsysback.entity.StampManagementEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.stampsysback.mapper.StampManagementMapper;

@RestController
@RequestMapping("/api/stamp-management")
@RequiredArgsConstructor
public class StampManagementController {

    private final StampManagementMapper stampManagementMapper;

    // 授業ごとのスタンプ一覧取得
    @GetMapping("/{classId}")
    public List<StampManagementEntity> getStamps(@PathVariable int classId) {
        return stampManagementMapper.findByClassId(classId);
    }

    // 新しいスタンプの追加
    @PostMapping
    public void addStamp(@RequestBody StampManagementEntity stamp) {
        stampManagementMapper.insert(stamp);
    }

    // スタンプの更新
    @PutMapping
    public void updateStamp(@RequestBody StampManagementEntity stamp) {
        stampManagementMapper.update(stamp);
    }

    // スタンプの削除
    @DeleteMapping("/{id}")
    public void deleteStamp(@PathVariable int id) {
        stampManagementMapper.delete(id);
    }
}
