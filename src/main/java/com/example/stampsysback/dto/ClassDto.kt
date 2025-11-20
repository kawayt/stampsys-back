package com.example.stampsysback.dto
import lombok.Data;
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
public class ClassDto {
    private Integer classId;
    private String className;
    private OffsetDateTime createdAt;
}