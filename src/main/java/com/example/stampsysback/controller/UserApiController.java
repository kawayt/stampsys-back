package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // role を受け取るように変更
    @GetMapping
    public Page<UserDto> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String role,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return userService.listUsersPage(q, role, page, size);
    }

    /**
     * 非表示ユーザー一覧（管理者のみ） - ページネーション対応
     */
    @GetMapping("/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> listHidden(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return userService.listHiddenUsersPage(q, page, size);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRole(@PathVariable("id") Integer id, @RequestBody RoleUpdateRequest req) {
        return userService.updateRole(id, req.getRole());
    }

//    @PutMapping("/{id}/hidden")
//    @PreAuthorize("hasRole('ADMIN')")
//    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody HiddenUpdateRequest req) {
//        return userService.updateHidden(id, req.isHidden());
//    }

    // counts に role を受け取るように変更
    @GetMapping("/counts")
    public UserCountsDto counts(@RequestParam(required = false) String role) {
        return userService.getUserCounts(role);
    }

    public static class RoleUpdateRequest {
        private String role;
        public String getRole(){ return role; }
        public void setRole(String role){ this.role = role; }
    }
    @PutMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody Map<String, Object> body) {
        boolean hidden = false;
        if (body != null && body.containsKey("hidden")) {
            Object v = body.get("hidden");
            hidden = Boolean.parseBoolean(String.valueOf(v));
        }
        return userService.updateHidden(id, hidden);
    }

    public static class HiddenUpdateRequest {
        private boolean hidden;
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
}