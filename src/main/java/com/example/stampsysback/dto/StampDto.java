package com.example.stampsysback.dto;

public class StampDto {
    private Integer stampId;
    private String stampName;
    private String stampColor;
    private String stampIcon;

    public Integer getStampId() { return stampId; }
    public void setStampId(Integer stampId) { this.stampId = stampId; }
    public String getStampName() { return stampName; }
    public void setStampName(String stampName) { this.stampName = stampName; }
    public String getStampColor() { return stampColor; }
    public void setStampColor(String stampColor) { this.stampColor = stampColor; }
    public String getStampIcon() { return stampIcon; }
    public void setStampIcon(String stampIcon) { this.stampIcon = stampIcon; }
}
