package com.example.stampsysback.controller;

import com.example.stampsysback.dto.ClassResponse;
import com.example.stampsysback.entity.ClassEntity;
import com.example.stampsysback.form.ClassForm;
import com.example.stampsysback.helper.ClassHelper;
import com.example.stampsysback.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClassController {
    // DI サービスに依存
    private final ClassService classService;

    // クラス一覧を表示
    @GetMapping("/list")
    public List<ClassEntity> list(){
        return classService.selectAllClass();
    }

    // 【追加】削除済みクラスを一覧表示
    @GetMapping("/deleted-list")
    public List<ClassEntity> deletedList(){
        return classService.selectDeletedClass();
    }

    // クラス新規作成
    @PostMapping
    public ClassResponse create(@RequestBody @Valid ClassForm classForm){
        // エンティティへ変換
        ClassEntity classEntity = ClassHelper.convertEntity(classForm);
        // 作成実行
        classService.insertClass(classEntity);
        return new ClassResponse(classEntity.getClassId(), classEntity.getClassName(), classEntity.getCreatedAt());
    }
}
