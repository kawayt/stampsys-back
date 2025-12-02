package com.example.stampsysback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ClassRoomsResponse {
    private ClassDto classInfo;  // クラス情報
    private List<RoomDto> rooms; // ルーム一覧
}