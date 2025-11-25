package com.example.stampsysback.entity;

import lombok.Data;

@Data
public class StampManagementEntity {
    private Integer stampId; // 主キー
    private String  stampName;
    private Integer stampIcon;
    private Integer stampColor;
    private boolean stampDeleted; // 論理削除フラグ

    private boolean assigned; // クラスで使用中かどうか DBの作成は必要ない
}
