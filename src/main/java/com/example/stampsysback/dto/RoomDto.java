package com.example.stampsysback.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
@Data
@NoArgsConstructor
// RoomEntity変換DTO
public class RoomDto {
    private Integer roomId;
    private String roomName;
    private Integer classId;
    private Boolean active;
    private OffsetDateTime createdAt;
}