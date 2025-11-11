package com.example.stampsysback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampSendRequest {//スタンプを押したときに送信されるデータ
    @NotNull
    private int userId;

    @NotNull
    private int stampId;

    @NotNull
    private int roomId;
}