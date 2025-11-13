package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.service.UserService;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Objects;

@Controller
public class UserWebController {

    private static final Logger logger = LoggerFactory.getLogger(UserWebController.class);

    private final UserRepository userRepository;
    private final UserService userService;

    public UserWebController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // 閲覧は認証済みユーザーなら誰でも可能（@PreAuthorize を外す）
    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal OAuth2User principal, Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        // OAuth2 attributes（テンプレートで参照したい場合）
        if (principal != null) {
            model.addAttribute("attributes", principal.getAttributes());
        }

        // 現在ログインしているユーザーの DB 上の User を model に追加（テンプレートで role 判定・表示のため）
        if (principal != null) {
            Object subObj = principal.getAttribute("sub");
            Object oidObj = principal.getAttribute("oid");
            Object idObj = principal.getAttribute("id");
            String providerId = firstNonNullString(subObj, oidObj, idObj);
            if (providerId != null) {
                userRepository.findByProviderUserId(providerId).ifPresent(u -> model.addAttribute("user", u));
            } else {
                String email = firstNonNullString(principal.getAttribute("email"));
                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(u -> model.addAttribute("user", u));
                }
            }
        }

        return "app"; // templates/app.html を返す
    }

    // 編集は ADMIN のみ
    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRole(@PathVariable("id") Integer id,
                             @RequestParam("role") String role,
                             Model model,
                             Authentication authentication) {
        // デバッグ: 呼び出し時の認可情報をログ出力
        if (authentication != null) {
            logger.info("updateRole called by principal={}, authorities={}",
                    authentication.getName(), authentication.getAuthorities());
        } else {
            logger.warn("updateRole called with null authentication");
        }

        try {
            userService.updateRole(id, role);
            return "redirect:/users";
        } catch (IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("targetUserId", id);
            model.addAttribute("requestedRole", role);
            return "admin_exists";
        }
    }

    @GetMapping("/users/admins/count")
    @ResponseBody
    public Long adminCount() {
        return userRepository.countByRole("ADMIN");
    }

    private static String firstNonNullString(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                return Objects.toString(o, null);
            }
        }
        return null;
    }
}
