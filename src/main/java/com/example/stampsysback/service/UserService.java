package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    List<UserDto> listUsers(String q);
    UserDto updateRole(Integer userId, String newRole);

    // 削除は廃止、非表示フラグのみ操作する
    UserDto updateHidden(Integer userId, boolean hidden);

    // 非表示フラグを考慮したロール別カウントを取得
    UserCountsDto getUserCounts();

    // ページネーション対応のユーザー一覧取得
    Page<UserDto> listUsersPage(String q, int page, int size);

    // 追加: 非表示ユーザーのページ取得（管理者向け）
    Page<UserDto> listHiddenUsersPage(String q, int page, int size);
}