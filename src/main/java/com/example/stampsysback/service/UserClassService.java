package com.example.stampsysback.service;

import com.example.stampsysback.dto.ClassDto;
import com.example.stampsysback.dto.RoomDto;

import java.util.List;

public interface UserClassService {

    // ユーザーをクラスに追加
    // return 1 = 新規に追加した、0 = 既に追加済みで何もしなかった
    int addUserToClass(Integer userId, Integer classId);

    // ユーザーをクラスから削除
    // return 1 = 削除した、0 = もともと存在しなかった
    int removeUserFromClass(Integer userId, Integer classId);


    // 指定ユーザーが指定クラスに所属しているか
    boolean isUserInClass(Integer userId, Integer classId);

    // 指定ユーザーが見えるクラス一覧
    List<ClassDto> getClassForUser(Integer userId);

    // 指定ユーザーが見えるルーム一覧
    List<RoomDto> getRoomForUser(Integer userId);

}
