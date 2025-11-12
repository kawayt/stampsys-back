package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;


//スタンプ送信処理の終了後、フロントに返す、スタンプを押せた証明のデータ
@Getter
@Setter
public class StampSendResponse {
    //スタンプが押せたことを確認するために必要
    private boolean success;
}
