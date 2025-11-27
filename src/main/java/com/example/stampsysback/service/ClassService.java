package com.example.stampsysback.service;

import com.example.stampsysback.entity.ClassEntity;

import java.util.List;

public interface ClassService {
    List<ClassEntity> selectAllClass();
    void insertClass(ClassEntity classEntity);

    // 【追加】論理削除
    void deleteClass(Integer classId);
    // 【追加】復元
    void restoreClass(Integer classId);
}
