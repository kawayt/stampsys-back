package com.example.stampsysback.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ClassEntity {
    private Integer classId;
    private String className;
    private OffsetDateTime createdAt;

    // 【修正】論理削除用のフィールドを Boolean 型に変更
    private Boolean hidden; // 削除フラグ (TRUE: 削除済み / FALSE: 未削除)
}
