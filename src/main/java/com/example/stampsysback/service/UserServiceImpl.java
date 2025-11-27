package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 実装クラス（@Service）。
 * 既存のメソッドはそのままに、getUserCounts(String) を実装しています。
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
        return listUsers(q, null);
    }

    // 既存の listUsers(q, role) 実装（省略しない形で入れてあります）
    public List<UserDto> listUsers(String q, String role) {
        List<User> users;
        if ((q == null || q.trim().isEmpty()) && (role == null || "ALL".equalsIgnoreCase(role))) {
            users = userRepository.findByHiddenFalse();
        } else {
            String keyword = q == null ? null : q.toLowerCase();
            users = userRepository.findAll().stream()
                    .filter(u -> !u.isHidden())
                    .filter(u -> {
                        if (role != null && !"ALL".equalsIgnoreCase(role)) {
                            if (u.getRole() == null) return false;
                            return u.getRole().equalsIgnoreCase(role);
                        }
                        return true;
                    })
                    .filter(u -> {
                        if (keyword == null || keyword.isEmpty()) return true;
                        return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword))
                                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                    })
                    .collect(Collectors.toList());
        }
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> listUsersPage(String q, int page, int size) {
        return listUsersPage(q, null, page, size);
    }

    @Override
    public Page<UserDto> listUsersPage(String q, String role, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());

        Page<User> pageResult;

        if ((q == null || q.trim().isEmpty()) && (role == null || "ALL".equalsIgnoreCase(role))) {
            pageResult = userRepository.findByHiddenFalse(pageable);
        } else if ((q == null || q.trim().isEmpty()) && role != null && !"ALL".equalsIgnoreCase(role)) {
            pageResult = userRepository.findByRoleAndHiddenFalse(role, pageable);
        } else {
            String keyword = q == null ? null : q.toLowerCase();
            List<User> filtered = userRepository.findAll().stream()
                    .filter(u -> !u.isHidden())
                    .filter(u -> {
                        if (role != null && !"ALL".equalsIgnoreCase(role)) {
                            return u.getRole() != null && u.getRole().equalsIgnoreCase(role);
                        }
                        return true;
                    })
                    .filter(u -> {
                        if (keyword == null || keyword.isEmpty()) return true;
                        return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword))
                                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                    })
                    .collect(Collectors.toList());
            int start = Math.min(filtered.size(), page * size);
            int end = Math.min(filtered.size(), start + size);
            List<User> content = filtered.subList(start, end);
            pageResult = new PageImpl<>(content, pageable, filtered.size());
        }

        return pageResult.map(this::toDto);
    }

    @Override
    public Page<UserDto> listHiddenUsersPage(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());
        if (q == null || q.trim().isEmpty()) {
            List<User> hiddenAll = userRepository.findByHiddenTrue();
            int start = Math.min(hiddenAll.size(), page * size);
            int end = Math.min(hiddenAll.size(), start + size);
            List<User> content = hiddenAll.subList(start, end);
            return new PageImpl<>(content.stream().map(this::toDto).collect(Collectors.toList()), pageable, hiddenAll.size());
        } else {
            String keyword = q.toLowerCase();
            List<User> filtered = userRepository.findByHiddenTrue().stream()
                    .filter(u ->
                            (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword)) ||
                                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
            int start = Math.min(filtered.size(), page * size);
            int end = Math.min(filtered.size(), start + size);
            List<User> content = filtered.subList(start, end);
            return new PageImpl<>(content.stream().map(this::toDto).collect(Collectors.toList()), pageable, filtered.size());
        }
    }

    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        String prevRole = u.getRole();
        if (newRole != null && "ADMIN".equalsIgnoreCase(newRole)) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (!(prevRole != null && prevRole.equalsIgnoreCase("ADMIN"))) {
                if (adminCount >= 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "管理者権限は最大２人を超えて付与することができません");
                }
            }
        }

        u.setRole(newRole);
        userRepository.save(u);
        return toDto(u);
    }

    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        if (!hidden) {
            String role = u.getRole();
            if (role != null && "ADMIN".equalsIgnoreCase(role)) {
                long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
                if (adminCount >= 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "管理者権限は最大２人を超えて付与することができません");
                }
            }
        }

        u.setHidden(hidden);
        userRepository.save(u);
        return toDto(u);
    }

    @Override
    public UserCountsDto getUserCounts() {
        return getUserCounts(null);
    }

    // ここでインターフェースの抽象メソッドをオーバーライドしている
    @Override
    public UserCountsDto getUserCounts(String role) {
        if (role != null && !"ALL".equalsIgnoreCase(role)) {
            long filtered = userRepository.countByRoleAndHiddenFalse(role);
            UserCountsDto dto = new UserCountsDto();
            if ("ADMIN".equalsIgnoreCase(role)) dto.setAdmin((int) filtered);
            else if ("TEACHER".equalsIgnoreCase(role)) dto.setTeacher((int) filtered);
            else if ("STUDENT".equalsIgnoreCase(role)) dto.setStudent((int) filtered);
            dto.setTotal((int) filtered);
            return dto;
        } else {
            long admin = userRepository.countByRoleAndHiddenFalse("ADMIN");
            long teacher = userRepository.countByRoleAndHiddenFalse("TEACHER");
            long student = userRepository.countByRoleAndHiddenFalse("STUDENT");
            long total = userRepository.countByHiddenFalse();
            UserCountsDto dto = new UserCountsDto();
            dto.setAdmin((int) admin);
            dto.setTeacher((int) teacher);
            dto.setStudent((int) student);
            dto.setTotal((int) total);
            return dto;
        }
    }

    private UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setUserId(u.getUserId());
        d.setUserName(u.getUserName());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole());
        d.setCreatedAt(u.getCreatedAt());
        d.setHidden(u.isHidden());
        return d;
    }

    @Override
    public Map<Integer, String> getUserNamesByIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<Integer> ids = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        final int BATCH_SIZE = 900;
        Map<Integer, String> result = new HashMap<>(Math.min(ids.size(), 256));

        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ids.size());
            List<Integer> chunk = ids.subList(i, end);
            Iterable<User> found = userRepository.findAllById(chunk);
            for (User u : found) {
                if (u == null || u.getUserId() == null) continue;
                result.putIfAbsent(u.getUserId(), u.getUserName() != null ? u.getUserName() : "");
            }
        }
        return result;
    }
}