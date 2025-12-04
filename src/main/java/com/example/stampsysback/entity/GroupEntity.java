package com.example.stampsysback.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "\"group\"") // 予約語回避
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ★重要: これを追加！
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "group_name")
    private String groupName;

    public GroupEntity() {
    }

    public Integer getGroupId() {
        return groupId;
    }
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}