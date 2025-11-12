package com.example.stampsysback.entity;

import lombok.Data;

@Data
public class StampManagementEntity {
    private Integer classId;

    private Integer stamp_id;
    private String stamp_name;
    private String stamp_icon;
    private String stamp_color;
}
