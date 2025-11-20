package com.example.stampsysback.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassDto {
    private Integer classId;
    private String className;
    private OffsetDateTime createdAt;
}