package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping
    public Page<UserDto> list(@RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return userService.listUsersPage(q, page, size);
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

    @PutMapping("/{id}/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody HiddenUpdateRequest req) {
        return userService.updateHidden(id, req.isHidden());
    }

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