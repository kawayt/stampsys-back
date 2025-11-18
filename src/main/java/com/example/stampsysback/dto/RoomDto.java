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

    // コンストラクタ、ゲッター、セッターはLombokの@Dataと@NoArgsConstructorで自動生成される
//    public RoomDto() {}
//
//    public Integer getRoomId() { return roomId; }
//    public void setRoomId(Integer roomId) { this.roomId = roomId; }
//
//    public String getRoomName() { return roomName; }
//    public void setRoomName(String roomName) { this.roomName = roomName; }
//
//    public Integer getClassId() { return classId; }
//    public void setClassId(Integer classId) { this.classId = classId; }
//
//    public Boolean getActive() { return active; }
//    public void setActive(Boolean active) { this.active = active; }
//
//    public OffsetDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

}
