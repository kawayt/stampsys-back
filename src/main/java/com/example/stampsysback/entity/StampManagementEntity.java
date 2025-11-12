package com.example.stampsysback.entity;

import lombok.Data;

@Data
public class StampManagementEntity {
    private Integer stampId;     // 主キー
    private String stampName;
    private String stampIcon;
    private String stampColor;
}
