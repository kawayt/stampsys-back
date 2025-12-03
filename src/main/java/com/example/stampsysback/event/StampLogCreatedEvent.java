package com.example.stampsysback.event;

import lombok.Getter;
import lombok.Setter;

/**
 * スタンプログの作成を表すイベント（トランザクション完了後にリスナーで処理する）
 * シンプルな POJO として定義します（Spring は任意のオブジェクトをイベントとして扱える）。
 */
@Getter
@Setter
public class StampLogCreatedEvent {
    private final Long roomId;

    public StampLogCreatedEvent(Long roomId) {
        this.roomId = roomId;
    }
}