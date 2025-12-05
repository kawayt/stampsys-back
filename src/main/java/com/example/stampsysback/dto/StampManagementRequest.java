package com.example.stampsysback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data; // @Data があれば Setter/Getter は自動生成されます

@Data
public class StampManagementRequest {

    // 【追加】自動生成されたキーを受け取るためのフィールド
    private Integer stampId;

    @NotBlank(message = "スタンプ名は必須項目です。")
    private String stampName;

    @NotNull(message = "アイコン選択は必須です")
    private Integer stampIcon;

    @NotNull(message = "スタンプ色選択は必須です")
    private Integer stampColor;

    private Integer userId;
}