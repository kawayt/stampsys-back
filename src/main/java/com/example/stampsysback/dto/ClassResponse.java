package com.example.stampsysback.dto;

import java.time.OffsetDateTime;

public record ClassResponse(Integer classId, String className,
                            OffsetDateTime createdAt) {

}
