package com.example.stampsysback.dto;

import java.time.OffsetDateTime;

public class UserDto {
    private Integer userId;
    private String userName;
    private String email;
    private String role;
    private String groupName;
    private OffsetDateTime createdAt;

    // 非表示フラグ
    private boolean hidden;

    // groupId を含める（フロントが group 名を引けるようにする）
    private Integer groupId;

    public UserDto() {}

    public UserDto(Integer userId, String userName, String email, String role, OffsetDateTime createdAt, boolean hidden, Integer groupId) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.hidden = hidden;
        this.groupId = groupId;
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

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}