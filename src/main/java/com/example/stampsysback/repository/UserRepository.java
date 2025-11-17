package com.example.stampsysback.repository;

import com.example.stampsysback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByEmail(String email);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String userName, String email, Pageable pageable);

    // role によるカウント（管理者が既に存在するか確認するため）
    long countByRole(String role);

    // hidden=false のユーザー一覧を取得するメソッド
    List<User> findByHiddenFalse();

    // ★ 追加: hidden=false を考慮した役割ごとのカウント
    long countByRoleAndHiddenFalse(String role);

    // ★ 追加: 表示される全体数（hidden=false の合計）
    long countByHiddenFalse();
}