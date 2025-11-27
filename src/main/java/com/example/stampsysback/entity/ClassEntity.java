package com.example.stampsysback.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ClassEntity {
    private Integer classId;
    private String className;
    private OffsetDateTime createdAt;

    // 【追加】論理削除用のフィールド
    private OffsetDateTime deletedAt; // 削除日時 (nullなら未削除)
}
