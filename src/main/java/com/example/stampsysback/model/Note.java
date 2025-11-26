package com.example.stampsysback.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Note {
    private Integer noteId;
    private String noteText;
    private Integer roomId;
    private Boolean hidden;
    private Timestamp createdAt;

    @Override
    public String toString() {
        return "Note{" +
                "noteId=" + noteId +
                ", noteText='" + noteText + '\'' +
                ", roomId=" + roomId +
                ", hidden=" + hidden +
                ", createdAt=" + createdAt +
                '}';
    }
}