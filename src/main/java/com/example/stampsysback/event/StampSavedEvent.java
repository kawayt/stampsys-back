package com.example.stampsysback.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * スタンプ保存（送信）完了を表すイベント。
 * roomId をプリミティブ long にして不要なボクシングを避ける。
 */
@Getter
@Setter
public class StampSavedEvent extends ApplicationEvent {

    private final long roomId;

    public StampSavedEvent(Object source, long roomId) {
        super(source);
        this.roomId = roomId;
    }
}