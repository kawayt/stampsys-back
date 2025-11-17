package com.example.stampsysback.controller;

import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

//    // 管理画面の /users などは別の Web コントローラで扱っている想定のため、
//    // ここでは削除エンドポイント等は提供しない（API 経由で hidden を切り替える方針）。
//    @GetMapping("/users")
//    public String usersPage(Model model) {
//        // 既存の Web コントローラの責務がある場合は view に必要な情報を詰める
//        // ただし、実際のユーザー一覧取得はフロント側から /api/users を呼ぶことを想定
//        return "app";
//    }
}