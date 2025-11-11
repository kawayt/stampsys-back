package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMapping;

@Getter
@Setter
public class StampSendRequest {
    private String userId;
    private int stampCount;
}