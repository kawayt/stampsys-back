package com.example.stampsysback.dto;
//ルームごとのスタンプ集計用のDTO

public class RoomStampCountDto {
    private Long roomId;
    private Long count;

    public RoomStampCountDto(Long roomId, Long count) {
        this.roomId = roomId;
        this.count = count;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}