package com.example.stampsysback.dto;

import lombok.Data;

/**
 * ルームごとのメモ件数を受け取る DTO
 */
@Data
public class NoteCount {
    private Integer roomId;
    private Integer noteCount;
}