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
import java.util.stream.Collectors;

/**
 * フロントエンド向けの /api/app コントローラ（改良版）
 *
 * 変更点:
 * - Student 権限のユーザーには users 一覧 (および管理系データ) を返さないようにしました。
 * - クライアントへ返すユーザー情報は最小限の DTO (UserDto) に変換して不要なフィールドを含めないようにしています。
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class AppController {

    private final UserRepository userRepository;

    public AppController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ログインユーザー情報 + 管理者/教員向けの管理データを返すエンドポイント。
     * - STUDENT の場合は users を含めません（null または空配列にできますが null にしています）。
     *
     * GET /api/app
     */
    @GetMapping("/api/app")
    public ResponseEntity<AppResponse> app(@AuthenticationPrincipal OAuth2User principal) {
        // 未ログインなら 401 を返す
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // OAuth2 attributes（そのまま JSON として返す）
        Map<String, Object> attributes = principal.getAttributes();

        // ログイン中ユーザーに紐づく DB 上の User を検索
        Object subObj = principal.getAttribute("sub");
        Object oidObj = principal.getAttribute("oid");
        Object idObj = principal.getAttribute("id");
        String providerId = firstNonNullString(subObj, oidObj, idObj);

        User dbUser = null;
        if (providerId != null) {
            dbUser = userRepository.findByProviderUserId(providerId).orElse(null);
        } else {
            String email = firstNonNullString(principal.getAttribute("email"));
            if (email != null) {
                dbUser = userRepository.findByEmail(email).orElse(null);
            }
        }

        // 判定: 管理者または教員かどうか
        boolean isAdminOrTeacher = dbUser != null && dbUser.getRole() != null &&
                (dbUser.getRole().equalsIgnoreCase("ADMIN") || dbUser.getRole().equalsIgnoreCase("TEACHER"));

        // 管理者／教員のみ users を含める。含める場合も DTO に変換して返す
        List<UserDto> usersDto = null;
        if (isAdminOrTeacher) {
            List<User> users = userRepository.findAll();
            usersDto = users.stream()
                    .map(AppController::toDto)
                    .collect(Collectors.toList());
        }

        // ログイン中ユーザーの情報も DTO として返す（null にはしない）
        UserDto currentUserDto = dbUser != null ? toDto(dbUser) : null;

        AppResponse body = new AppResponse(attributes, usersDto, currentUserDto);
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

    private static UserDto toDto(User u) {
        if (u == null) return null;
        return new UserDto(
                u.getUserId(),
                u.getUserName(),
                u.getEmail(),
                u.getRole(),
                u.getProviderUserId(),
                u.isHidden()
        );
    }

    /**
     * React 向けのレスポンス DTO
     */
    public static class AppResponse {
        private Map<String, Object> attributes;
        private List<UserDto> users; // 管理者/教員のみ値が入る（STUDENT は null）
        private UserDto user; // ログイン中ユーザー（見つかった場合のみ）

        public AppResponse(Map<String, Object> attributes, List<UserDto> users, UserDto user) {
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

        public List<UserDto> getUsers() {
            return users;
        }
        public void setUsers(List<UserDto> users) {
            this.users = users;
        }

        public UserDto getUser() {
            return user;
        }
        public void setUser(UserDto user) {
            this.user = user;
        }
    }

    /**
     * フロントに渡す最小限のユーザー DTO（不要フィールドを含めない）
     */
    public static class UserDto {
        private Integer userId;
        private String userName;
        private String email;
        private String role;
        private String providerUserId;
        private boolean hidden;

        public UserDto(Integer userId, String userName, String email, String role, String providerUserId, boolean hidden) {
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.role = role;
            this.providerUserId = providerUserId;
            this.hidden = hidden;
        }

        public Integer getUserId() {
            return userId;
        }
        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }

        public String getProviderUserId() {
            return providerUserId;
        }
        public void setProviderUserId(String providerUserId) {
            this.providerUserId = providerUserId;
        }

        public boolean isHidden() {
            return hidden;
        }
        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }
    }
}