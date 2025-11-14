package com.example.stampsysback.helper;

import com.example.stampsysback.entity.ClassEntity;
import com.example.stampsysback.form.ClassForm;

import java.time.OffsetDateTime;

public class ClassHelper {
    // ClassForm → ClassEntity に変換
    public static ClassEntity convertEntity(ClassForm classForm){
        ClassEntity classEntity = new ClassEntity();
        classEntity.setClassName(classForm.getClassName());
        classEntity.setCreatedAt(OffsetDateTime.now());
        return classEntity;
    }
}
