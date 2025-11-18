package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * シンプル実装。既存の変換ロジックがあればそれに合わせて修正してください。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> listUsers(String q) {
        // 非表示ではないユーザーだけを返す。
        List<User> users;
        if (q == null || q.trim().isEmpty()) {
            users = userRepository.findByHiddenFalse();
        } else {
            String keyword = q.toLowerCase();
            users = userRepository.findAll().stream()
                    .filter(u -> !u.isHidden()) // hidden = false のみ
                    .filter(u ->
                            (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword)) ||
                                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
        }
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * ページネーション対応のユーザー一覧取得
     */
    @Override
    public Page<UserDto> listUsersPage(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());
        Page<User> pageResult;
        if (q == null || q.trim().isEmpty()) {
            pageResult = userRepository.findByHiddenFalse(pageable);
        } else {
            pageResult = userRepository.searchVisible(q, pageable);
        }
        return pageResult.map(this::toDto);
    }

    /**
     * ロール更新
     * - 最後の表示中 ADMIN を削除/降格させない保護を追加
     * - 管理者の最大人数制限（ここでは最大2名）に達している場合は追加を拒否する
     */
    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1) 管理者の追加制限: newRole が ADMIN への昇格で、かつ現在 ADMIN でない場合
        if ("ADMIN".equals(newRole) && !"ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            // 既に表示中の ADMIN が 2 人以上いる場合は追加を拒否
            if (adminCount >= 2) {
                throw new IllegalStateException("管理者は最大2名までに制限されています。これ以上管理者を追加できません。");
            }
        }

        // 2) 降格保護: 現在 ADMIN を別のロールにしようとする場合、最後の一人でないか確認
        if ("ADMIN".equals(user.getRole()) && !"ADMIN".equals(newRole)) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            // adminCount は表示中の ADMIN の数。自分が最後の一人なら降格を拒否
            if (adminCount <= 1) {
                throw new IllegalStateException("最後の管理者削除することはできません。最低でも管理者は１人存在している必要があります。");
            }
        }

        user.setRole(newRole);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    /**
     * 非表示フラグ更新（物理削除はしない）
     * - ADMIN を非表示にする際、最後の表示中 ADMIN を隠せないよう保護
     */
    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 重要: ADMIN を非表示にしようとする際は最後の表示中の ADMIN にならないか確認
        if (hidden && "ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (adminCount <= 1) {
                throw new IllegalStateException("最後の管理者削除することはできません。最低でも管理者は１人存在している必要があります。");
            }
        }

        user.setHidden(hidden);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    public UserCountsDto getUserCounts() {
        long admin = userRepository.countByRoleAndHiddenFalse("ADMIN");
        long teacher = userRepository.countByRoleAndHiddenFalse("TEACHER");
        long student = userRepository.countByRoleAndHiddenFalse("STUDENT");
        long total = userRepository.countByHiddenFalse();
        return new UserCountsDto(admin, teacher, student, total);
    }

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setUserId(u.getUserId());
        dto.setUserName(u.getUserName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setHidden(u.isHidden()); // hidden を DTO に含める
        return dto;
    }
}