package com.example.stampsysback.dto;

import lombok.Data;

@Data
public class StampManagementResponse {
    private Integer stampId;
    private String  stampName;
    private Integer stampIcon;
    private Integer stampColor;
    private Integer userId;
    private boolean assigned; // フロント用
}
