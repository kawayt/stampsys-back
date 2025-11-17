package com.example.stampsysback.controller;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<UserDto> list(@RequestParam(required = false) String q) {
        return (List<UserDto>) userService.listUsers(q);
    }

    @PutMapping("/{id}/role")
    public UserDto updateRole(@PathVariable("id") Integer id, @RequestBody RoleUpdateRequest req) {
        // TODO: 管理者権限チェック（Spring Security）
        return userService.updateRole(id, req.getRole());
    }

    // 非表示状態の更新（hidden=true が「実質削除」）
    @PutMapping("/{id}/hidden")
    public UserDto updateHidden(@PathVariable("id") Integer id, @RequestBody HiddenUpdateRequest req) {
        // TODO: 管理者権限チェック（Spring Security）
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