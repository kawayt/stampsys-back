package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampSendRequest {
    private String userId;
    private int stampCount;
}