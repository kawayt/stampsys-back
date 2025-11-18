package com.example.stampsysback.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomForm {
    @NotBlank(message = "roomName は必須です")
    private String roomName;

    @NotNull
    private Integer classId;

    private Boolean active;
}
