package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.service.UserService;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ブラウザで users テーブルの中身を確認するための簡易 Web コントローラ
 */
@Controller
public class UserWebController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserWebController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "app"; // templates/app.html を返す
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable("id") Integer id, @RequestParam("role") String role) {
        // サーバサイドの動作確認用。実運用では管理者チェックを忘れずに。
        userService.updateRole(id, role);
        return "redirect:/users";
    }
}