package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ユーザー一覧取得 (GET /api/users)
     * 検索、ロール絞り込み、所属グループ絞り込み(groupId)に対応
     */
    @GetMapping("/api/users")
    public Page<UserDto> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer groupId, // ★追加: 所属で絞り込み
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return userService.listUsersPage(q, role, groupId, page, size);
    }

    /**
     * 非表示ユーザー一覧 (GET /api/users/hidden)
     */
    @GetMapping("/api/users/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> listHidden(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return userService.listHiddenUsersPage(q, page, size);
    }

    /**
     * ユーザー数カウント (GET /api/users/counts)
     */
    @GetMapping("/api/users/counts")
    public UserCountsDto counts(@RequestParam(required = false) String role) {
        return userService.getUserCounts(role);
    }

    /**
     * 所属グループ変更 (PUT /api/users/{userId}/group)
     */
    @PutMapping("/api/users/{userId}/group")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUserGroup(@PathVariable Integer userId, @RequestBody Map<String, Integer> body) {
        Integer newGroupId = body.get("groupId");
        return userService.updateGroup(userId, newGroupId);
    }

    /**
     * ロール変更 (PUT /api/users/{id}/role)
     */
    @PutMapping("/api/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRole(@PathVariable("id") Integer id, @RequestBody RoleUpdateRequest req) {
        return userService.updateRole(id, req.getRole());
    }

    /**
     * 非表示フラグ変更 (PUT /api/users/{id}/hidden)
     */
    @PutMapping("/api/users/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody Map<String, Object> body) {
        boolean hidden = false;
        if (body != null && body.containsKey("hidden")) {
            Object v = body.get("hidden");
            hidden = Boolean.parseBoolean(String.valueOf(v));
        }
        return userService.updateHidden(id, hidden);
    }
    @GetMapping("/api/users/counts/groups")
    public Map<Integer, Long> getGroupCounts() {
        return userService.getUserCountsByGroup();
    }

    // 内部クラス: リクエストボディ用
    public static class RoleUpdateRequest {
        private String role;
        public String getRole(){ return role; }
        public void setRole(String role){ this.role = role; }
    }
}