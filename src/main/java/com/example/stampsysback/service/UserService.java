package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import java.util.List;

public interface UserService {
    List<UserDto> listUsers(String q);
    UserDto updateRole(Integer userId, String newRole);

    // 削除は廃止、非表示フラグのみ操作する
    UserDto updateHidden(Integer userId, boolean hidden);

    // ★ 追加: 非表示フラグを考慮したロール別カウントを取得
    UserCountsDto getUserCounts();
}