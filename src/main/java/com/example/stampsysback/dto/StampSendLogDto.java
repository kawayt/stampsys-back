package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class StampSendLogDto {
    private Integer stampId;
    private String stampName;
    private String stampColor;
    private String stampIcon;
    private Instant sentAt;
}