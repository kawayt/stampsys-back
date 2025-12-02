package com.example.stampsysback.service;

import com.example.stampsysback.entity.ClassEntity;

import java.util.List;

public interface ClassService {
    List<ClassEntity> selectAllClass();
    void insertClass(ClassEntity classEntity);

    // classId を指定して1件取得
    ClassEntity selectClassById(Integer classId);

    // 論理削除
    void deleteClass(Integer classId);
    // 復元
    void restoreClass(Integer classId);
}
