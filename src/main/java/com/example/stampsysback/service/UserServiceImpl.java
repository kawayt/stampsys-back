package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getUserId(), u.getUserName(), u.getEmail(), u.getRole(), u.getCreatedAt());
    }

    @Override
    public Page<UserDto> listUsers(int page, int size, String sortBy, String direction, String q) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> p;
        if (q == null || q.isBlank()) {
            p = userRepository.findAll(pageable);
        } else {
            p = userRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        }
        return p.map(this::toDto);
    }

    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        u.setRole(newRole);
        userRepository.saveAndFlush(u);
        return toDto(u);
    }
}