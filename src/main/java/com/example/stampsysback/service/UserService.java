package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import java.util.List;

public interface UserService {
    List<UserDto> listUsers(String q);
    UserDto updateRole(Integer userId, String newRole);

    // 追加: ユーザー削除（IDで削除して成功なら true、存在しなければ false を返す）
    boolean deleteUser(Integer userId);
}