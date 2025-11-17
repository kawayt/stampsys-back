package com.example.stampsysback.entity;

import lombok.Data;

@Data
public class StampManagementEntity {
    private Integer stampId;     // 主キー
    private String stampName;
    private Integer stampIcon;  // 変更後 (DBの icon_id カラムに対応)
    private Integer stampColor; // 変更後 (DBの color_id カラムに対応)
}
