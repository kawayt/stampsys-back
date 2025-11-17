package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
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
        // 既存実装に合わせて置き換えてください。ここでは簡易実装を例示。
        // Pageable 等を使った実装がある場合はそちらに合わせること。
        List<User> users;
        if (q == null || q.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            // findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase を使う場合は Pageable の扱いに注意
            users = userRepository.findAll().stream()
                    .filter(u -> (u.getUserName() != null && u.getUserName().toLowerCase().contains(q.toLowerCase()))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q.toLowerCase())))
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
    public boolean deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }
        userRepository.deleteById(userId);
        return true;
    }

    private UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setUserId(u.getUserId());
        dto.setUserName(u.getUserName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}