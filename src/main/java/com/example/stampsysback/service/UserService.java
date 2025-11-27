package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    // 追加: role を考慮したページ取得（フロントから role クエリが来る想定）
    Page<UserDto> listUsersPage(String q, String role, int page, int size);

    // 追加: role を考慮した counts 取得
    UserCountsDto getUserCounts(String role);

    /**
     * 指定された userId の集合に対して display name を取得して返す。
     * 実装は DB へのバッチクエリ、キャッシュ、または外部ユーザサービス呼び出しのいずれかとする。
     *
     * @param userIds 取得対象の user_id 集合
     * @return userId -> displayName のマップ（見つからない userId はマップに含めない or null 値にするかは実装で統一）
     */
    Map<Integer, String> getUserNamesByIds(Collection<Integer> userIds);
}