package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {
    private String noteText;
    private Integer roomId;
}