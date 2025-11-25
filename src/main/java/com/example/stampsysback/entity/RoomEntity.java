package com.example.stampsysback.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RoomEntity {
    private Integer roomId;
    private String roomName;
    private Integer classId;
    private OffsetDateTime createdAt;
    private Boolean active;
    private Boolean hidden;
}
