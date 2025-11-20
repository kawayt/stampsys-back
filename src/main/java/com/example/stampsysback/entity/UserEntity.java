package com.example.stampsysback.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserEntity {
    private Integer userId;
    private String userName;
    private String providerUserId;
    private String email;
    private String role;
    private OffsetDateTime createdAt;
}
