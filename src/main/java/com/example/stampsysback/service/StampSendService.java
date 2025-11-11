package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampSendRequest;
import com.example.stampsysback.entity.StampSendRecord;

public interface StampSendService {
    StampSendRecord saveStamp(Integer userId, StampSendRequest stampSendRequest);
}
