package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * フロントエンド（Vite + React）向けの /api/app 用 REST コントローラ
 * ログイン中ユーザーの情報と、ユーザー一覧などを JSON で返す。
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AppController {

    private final UserRepository userRepository;

    public AppController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ログインユーザー情報 + ユーザー一覧などを返すエンドポイント。
     * GET /api/app
     */
    @GetMapping("/api/app")
    public ResponseEntity<AppResponse> app(@AuthenticationPrincipal OAuth2User principal) {
        // 未ログインなら 401 を返す（フロントでログイン画面に飛ばすなどの処理を行う）
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // OAuth2 attributes（そのまま JSON として返す）
        Map<String, Object> attributes = principal.getAttributes();

        // DB のユーザー一覧
        List<User> users = userRepository.findAll();

        // ログイン中ユーザーに紐づく DB 上の User を検索
        Object subObj = principal.getAttribute("sub");
        Object oidObj = principal.getAttribute("oid");
        Object idObj = principal.getAttribute("id");
        String providerId = firstNonNullString(subObj, oidObj, idObj);

        User currentUser = null;
        if (providerId != null) {
            currentUser = userRepository.findByProviderUserId(providerId).orElse(null);
        } else {
            String email = firstNonNullString(principal.getAttribute("email"));
            if (email != null) {
                currentUser = userRepository.findByEmail(email).orElse(null);
            }
        }

        AppResponse body = new AppResponse(attributes, users, currentUser);
        return ResponseEntity.ok(body);
    }

    private static String firstNonNullString(Object... objs) {
        for (Object o : objs) {
            if (o != null) {
                return Objects.toString(o, null);
            }
        }
        return null;
    }

    /**
     * React 向けのレスポンス DTO
     */
    public static class AppResponse {
        private Map<String, Object> attributes;
        private List<User> users;
        private User user; // ログイン中ユーザー（見つかった場合のみ）

        public AppResponse(Map<String, Object> attributes, List<User> users, User user) {
            this.attributes = attributes;
            this.users = users;
            this.user = user;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }
        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public List<User> getUsers() {
            return users;
        }
        public void setUsers(List<User> users) {
            this.users = users;
        }

        public User getUser() {
            return user;
        }
        public void setUser(User user) {
            this.user = user;
        }
    }
}