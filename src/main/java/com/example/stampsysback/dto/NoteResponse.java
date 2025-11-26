package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class NoteResponse {
    private Integer noteId;
    private String noteText;
    private Integer roomId;
    private Boolean hidden;
    private Timestamp createdAt;
}