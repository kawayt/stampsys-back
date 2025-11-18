package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

/**
 * API コントローラ: /api/users
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserApiController {

    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ページネーション対応のユーザー一覧
     * クエリ:
     *  - q: 検索キーワード（省略可）
     *  - page: 0 始まりのページ番号（デフォルト 0）
     *  - size: 1ページあたりの件数（デフォルト 20）
     */
    @GetMapping
    public Page<UserDto> list(@RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return userService.listUsersPage(q, page, size);
    }

    /**
     * ロール変更：管理者のみ許可
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRole(@PathVariable("id") Integer id, @RequestBody RoleUpdateRequest req) {
        return userService.updateRole(id, req.getRole());
    }

    // 非表示状態の更新（hidden=true が「実質削除」）
    @PutMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody HiddenUpdateRequest req) {
        return userService.updateHidden(id, req.isHidden());
    }

    // ロール別カウント（表示対象のみ）
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