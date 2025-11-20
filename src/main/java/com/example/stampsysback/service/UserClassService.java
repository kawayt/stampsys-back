package com.example.stampsysback.service;

public interface UserClassService {

    // ユーザーをクラスに追加
    // return 1 = 新規に追加した、0 = 既に追加済みで何もしなかった
    int addUserToClass(Integer userId, Integer classId);

    // ユーザーをクラスから削除
    // return 1 = 削除した、0 = もともと存在しなかった
    int removeUserFromClass(Integer userId, Integer classId);

    }
