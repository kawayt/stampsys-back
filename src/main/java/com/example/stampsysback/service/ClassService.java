package com.example.stampsysback.service;

import com.example.stampsysback.entity.ClassEntity;

import java.util.List;

public interface ClassService {
    List<ClassEntity> selectAllClass();
    void insertClass(ClassEntity classEntity);
}
