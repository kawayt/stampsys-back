package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API コントローラ: /api/users
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserApiController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserApiController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * ユーザー一覧（既存）：検索 q を受け取り、非表示フラグが false のユーザーのみ返す
     */
    @GetMapping
    public List<UserDto> list(@RequestParam(required = false) String q) {
        return (List<UserDto>) userService.listUsers(q);
    }

    /**
     * 追加エンドポイント: 非表示（hidden=true）のユーザー一覧を返す。
     * 管理者のみアクセス可能にしておくのが安全です。
     *
     * 使い方:
     *  GET /api/users/hidden
     *  GET /api/users/hidden?q=keyword   // 名前・メールで絞り込み
     */
    @GetMapping("/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listHidden(@RequestParam(required = false) String q) {
        List<User> users = userRepository.findAll().stream()
                .filter(User::isHidden)
                .collect(Collectors.toList());

        if (q != null && !q.trim().isEmpty()) {
            String keyword = q.toLowerCase();
            users = users.stream().filter(u ->
                    (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
            ).collect(Collectors.toList());
        }

        return users.stream().map(u -> {
            UserDto dto = new UserDto();
            dto.setUserId(u.getUserId());
            dto.setUserName(u.getUserName());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole());
            dto.setCreatedAt(u.getCreatedAt());
            dto.setHidden(u.isHidden());
            return dto;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}/role")
    public UserDto updateRole(@PathVariable("id") Integer id, @RequestBody RoleUpdateRequest req) {
        // TODO: 管理者権限チェック（Spring Security）
        return userService.updateRole(id, req.getRole());
    }

    // 非表示状態の更新（hidden=true が「実質削除」）
    @PutMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody HiddenUpdateRequest req) {
        return userService.updateHidden(id, req.isHidden());
    }

    // ★ 追加: ロール別カウント（表示対象のみ）
    @GetMapping("/counts")
    public UserCountsDto counts() {
        return userService.getUserCounts();
    }

    public static class RoleUpdateRequest {
        private String role;
        public String getRole(){ return role; }
        public void setRole(String role){ this.role = role; }
    }

    public static class HiddenUpdateRequest {
        private boolean hidden;
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
}