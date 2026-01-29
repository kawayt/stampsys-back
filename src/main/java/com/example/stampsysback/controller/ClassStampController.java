package com.example.stampsysback.controller;

import com.example.stampsysback.entity.ClassStampEntity;
import com.example.stampsysback.entity.StampManagementEntity;
import com.example.stampsysback.mapper.ClassStampMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class-stamps")
public class ClassStampController {

    private final ClassStampMapper classStampMapper;

    // 【設定画面用】全スタンプを取得し、指定クラスでONになっているものは isAssigned=true にする
    // フロントエンドはこれを受け取って表示するだけでOK
    @GetMapping("/manage-list/{classId}")
    public List<StampManagementEntity> getStampsForManagement(@PathVariable Integer classId) {
        return classStampMapper.findAllWithAssignmentStatus(classId);
    }

    // クラスに紐づくスタンプ一覧取得
    @GetMapping("/{classId}")
    public List<StampManagementEntity> getStampsByClass(@PathVariable Integer classId) {
        return classStampMapper.findByClassId(classId);
    }

    // クラスにスタンプを割り当てる
    @PostMapping
    public void assignStamp(@RequestBody ClassStampEntity classStamp) {
        classStampMapper.assignStampToClass(classStamp);
    }

    // クラスからスタンプの割り当てを解除
    @DeleteMapping
    public void removeStamp(@RequestBody ClassStampEntity classStamp) {
        classStampMapper.removeStampFromClass(classStamp);
    }

}
