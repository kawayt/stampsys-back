package com.example.stampsysback.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ClassEntity {
    private Integer classId;
    private String className;
    private OffsetDateTime createdAt;
}
