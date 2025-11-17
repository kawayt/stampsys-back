package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StampSummary {
    private Integer stampId;
    private String stampName;
    private Integer stampColor;
    private Integer stampIcon;
    private Integer cnt;
    private BigDecimal pct;
}