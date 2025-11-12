package com.example.stampsysback.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class StampSendRecord {//stamp_logsテーブルに登録される情報
    @NotNull
    private int stampLogId;
    private int userId;
    private int roomId;
    private int stampId;
    private Instant sentAt;// サービス側 or DBでセット
}
