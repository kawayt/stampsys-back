package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * StampLogDto: StampLog レコードの DTO
 * 既存フィールドに加えて、stampName/stampColor/stampIcon を追加します。
 * DB のデータ型に合わせて型は調整してください（以下は一例）。
 */
@Getter
@Setter
public class StampLogDto {
    private Long stampLogId;
    private Long roomId;
    private Integer userId;
    private Integer stampId;
    private OffsetDateTime sentAt;
    private String senderName;

    // 追加：スタンプ定義情報
    private String stampName;
    private Integer stampColor;
    private Integer stampIcon;

    //　group情報
    private Integer groupId;
    private String groupName;

    // 互換性のために id のアクセサを用意している場合は必要に応じて追加
    public Long getId() { return this.stampLogId; }
    public void setId(Long id) { this.stampLogId = id; }
}