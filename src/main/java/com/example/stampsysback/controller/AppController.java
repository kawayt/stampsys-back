package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Objects;

/**
 * /app に対応するコントローラ（ログイン後ダッシュボード）
 */
@Controller
public class AppController {

    private final UserRepository userRepository;

    public AppController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/app")
    public String app(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/";
        }

        // OAuth2 attributes（テンプレートで参照したい場合に渡す）
        model.addAttribute("attributes", principal.getAttributes());

        // DB のユーザー（一覧）を渡す。null にならないよう必ずセットする
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        // 連携用の providerId などで個人の user を渡したければ追加で検索する
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

        return "app"; // src/main/resources/templates/app.html をレンダリング
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