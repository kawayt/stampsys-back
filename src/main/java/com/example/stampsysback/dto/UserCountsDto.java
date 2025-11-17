package com.example.stampsysback.dto;

/**
 * ユーザー一覧向けのロール別カウント DTO (表示対象のみ: hidden=false)
 */
public class UserCountsDto {
    private long admin;
    private long teacher;
    private long student;
    private long total;

    public UserCountsDto() {}

    public UserCountsDto(long admin, long teacher, long student, long total) {
        this.admin = admin;
        this.teacher = teacher;
        this.student = student;
        this.total = total;
    }

    public long getAdmin() {
        return admin;
    }
    public void setAdmin(long admin) {
        this.admin = admin;
    }
    public long getTeacher() {
        return teacher;
    }
    public void setTeacher(long teacher) {
        this.teacher = teacher;
    }
    public long getStudent() {
        return student;
    }
    public void setStudent(long student) {
        this.student = student;
    }
    public long getTotal() {
        return total;
    }
    public void setTotal(long total) {
        this.total = total;
    }
}