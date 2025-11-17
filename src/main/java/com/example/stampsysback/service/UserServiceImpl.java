package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
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