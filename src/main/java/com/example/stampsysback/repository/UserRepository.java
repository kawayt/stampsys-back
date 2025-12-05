package com.example.stampsysback.repository;

import com.example.stampsysback.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // 追加
import org.springframework.data.jpa.repository.Query;     // 追加
import org.springframework.data.repository.query.Param;   // 追加

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findByHiddenFalse(Pageable pageable);
    List<User> findByHiddenFalse();

    // 追加: role と hidden を組み合わせた検索
    Page<User> findByRoleAndHiddenFalse(String role, Pageable pageable);
    List<User> findByRoleAndHiddenFalse(String role);

    // 非表示ユーザー
    List<User> findByHiddenTrue();
    Page<User> findByRoleAndHiddenTrue(String role, Pageable pageable);

    // カウント用
    long countByHiddenFalse();
    long countByRoleAndHiddenFalse(String role);

    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByEmail(String email);

    Long countByRole(String admin);

    // 既存: キーワード検索（実装済みがあればそのまま）
    // Page<User> searchVisible(String q, Pageable pageable); // もし存在するなら利用
    @Modifying
    @Query("UPDATE User u SET u.groupId = NULL WHERE u.groupId = :groupId")
    void setGroupIdToNull(@Param("groupId") Integer groupId);

}