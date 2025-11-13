package com.example.stampsysback.dto;

import java.time.OffsetDateTime;

public class UserDto {
    private Integer userId;
    private String userName;
    private String email;
    private String role;
    private OffsetDateTime createdAt;

    public UserDto() {}

    public UserDto(Integer userId, String userName, String email, String role, OffsetDateTime createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}