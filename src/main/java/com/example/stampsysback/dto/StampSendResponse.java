package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampSendResponse {
    private boolean success;//スタンプが押せたことを確認するために必要
    private String message;//「次のスタンプまで○○秒」の形で、スタンプ送信後に表示
}
