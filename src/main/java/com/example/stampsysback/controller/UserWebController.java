package com.example.stampsysback.controller;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * ブラウザで users テーブルの中身を確認するための簡易 Web コントローラ
 */
@Controller
public class UserWebController {

    private final UserRepository userRepository;

    public UserWebController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "app"; // templates/app.html を返す
    }
}
