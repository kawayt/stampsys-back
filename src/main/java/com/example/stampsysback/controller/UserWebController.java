package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.service.UserService;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String updateRole(@PathVariable("id") Integer id, @RequestParam("role") String role, Model model) {
        // サーバサイドの動作確認用。実運用では管理者チェックを忘れずに。
        try {
            userService.updateRole(id, role);
            return "redirect:/users";
        } catch (IllegalStateException ex) {
            // 管理者が既に存在するなどの理由で更新を拒否した場合は専用ページへ
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("targetUserId", id);
            model.addAttribute("requestedRole", role);
            return "admin_exists"; // templates/admin_exists.html を返す
        }
    }

    // オプション: 現在の ADMIN 数をクライアント側から問い合わせたいとき用の簡易 API
    @GetMapping("/users/admins/count")
    @ResponseBody
    public Long adminCount() {
        return userRepository.countByRole("ADMIN");
    }
}