package com.example.stampsysback.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

//スタンプを押したときに送信されるデータ
//フロントエンドで画面上のスタンプを押したとき、誰が何のスタンプをどのルームで押したかというデータを受け取る
@Getter
@Setter
public class StampSendRequest {
    @NotNull
    private int userId;

    @NotNull
    private int stampId;

    @NotNull
    private int roomId;
}