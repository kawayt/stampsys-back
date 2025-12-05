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

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ★必須メソッド: リスト取得（検索のみ）
    @Override
    public List<UserDto> listUsers(String q) {
        return listUsers(q, null);
    }

    // 内部用オーバーロード: リスト取得（検索 + ロール）
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
        }
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ★必須メソッド: ページネーション（検索のみ）
    @Override
    public Page<UserDto> listUsersPage(String q, int page, int size) {
        return listUsersPage(q, null, null, page, size);
    }

    // ★必須メソッド: ページネーション（検索 + ロール）
    @Override
    public Page<UserDto> listUsersPage(String q, String role, int page, int size) {
        return listUsersPage(q, role, null, page, size);
    }

    // ★必須メソッド: メイン検索ロジック（検索 + ロール + 所属）
    @Override
    public Page<UserDto> listUsersPage(String q, String role, Integer groupId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());

        List<User> allUsers = userRepository.findAll();
        String keyword = (q == null || q.trim().isEmpty()) ? null : q.toLowerCase();

        List<User> filtered = allUsers.stream()
                .filter(u -> !u.isHidden())
                .filter(u -> {
                    if (role != null && !"ALL".equalsIgnoreCase(role)) {
                        return u.getRole() != null && u.getRole().equalsIgnoreCase(role);
                    }
                    return true;
                })
                .filter(u -> {
                    // 所属(groupId)による絞り込み
                    if (groupId != null) {
                        // -1 の場合は「未所属(null)」を対象にする
                        if (groupId == -1) {
                            return u.getGroupId() == null;
                        }
                        return u.getGroupId() != null && u.getGroupId().equals(groupId);
                    }
                    return true;
                })
                .filter(u -> {
                    if (keyword == null) return true;
                    return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());

        int start = Math.min(filtered.size(), (int) pageable.getOffset());
        int end = Math.min(filtered.size(), start + pageable.getPageSize());
        List<User> content = filtered.subList(start, end);

        return new PageImpl<>(content.stream().map(this::toDto).collect(Collectors.toList()), pageable, filtered.size());
    }

    // ★必須メソッド: 非表示ユーザー一覧
    @Override
    public Page<UserDto> listHiddenUsersPage(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());
        String keyword = (q == null || q.trim().isEmpty()) ? null : q.toLowerCase();

        List<User> hiddenUsers = userRepository.findByHiddenTrue();

        List<User> filtered = hiddenUsers.stream()
                .filter(u -> {
                    if (keyword == null) return true;
                    return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());

        int start = Math.min(filtered.size(), (int) pageable.getOffset());
        int end = Math.min(filtered.size(), start + pageable.getPageSize());
        List<User> content = filtered.subList(start, end);

        return new PageImpl<>(content.stream().map(this::toDto).collect(Collectors.toList()), pageable, filtered.size());
    }

    // ★必須メソッド: ロール更新
    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        String prevRole = u.getRole();
        if (newRole != null && "ADMIN".equalsIgnoreCase(newRole)) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (!"ADMIN".equalsIgnoreCase(prevRole)) {
                if (adminCount >= 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理者権限は最大２人までです");
                }
            }
        }
        u.setRole(newRole);
        userRepository.save(u);
        return toDto(u);
    }

    // ★必須メソッド: 非表示更新
    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        u.setHidden(hidden);
        userRepository.save(u);
        return toDto(u);
    }

    // ★必須メソッド: ユーザー数カウント (引数なし)
    @Override
    public UserCountsDto getUserCounts() {
        return getUserCounts(null);
    }

    // ★必須メソッド: ユーザー数カウント (引数あり)
    @Override
    public UserCountsDto getUserCounts(String role) {
        UserCountsDto dto = new UserCountsDto();
        if (role != null && !"ALL".equalsIgnoreCase(role)) {
            long filtered = userRepository.countByRoleAndHiddenFalse(role);
            if ("ADMIN".equalsIgnoreCase(role)) dto.setAdmin((int) filtered);
            else if ("TEACHER".equalsIgnoreCase(role)) dto.setTeacher((int) filtered);
            else if ("STUDENT".equalsIgnoreCase(role)) dto.setStudent((int) filtered);
            dto.setTotal((int) filtered);
        } else {
            long admin = userRepository.countByRoleAndHiddenFalse("ADMIN");
            long teacher = userRepository.countByRoleAndHiddenFalse("TEACHER");
            long student = userRepository.countByRoleAndHiddenFalse("STUDENT");
            long total = userRepository.countByHiddenFalse();
            dto.setAdmin((int) admin);
            dto.setTeacher((int) teacher);
            dto.setStudent((int) student);
            dto.setTotal((int) total);
        }
        return dto;
    }

    // ★必須メソッド: グループ更新
    @Override
    @Transactional
    public UserDto updateGroup(Integer userId, Integer groupId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        u.setGroupId(groupId);
        userRepository.save(u);
        return toDto(u);
    }

    // ★必須メソッド: IDリストから名前取得
    @Override
    public Map<Integer, String> getUserNamesByIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<Integer> ids = userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, String> result = new HashMap<>();
        final int BATCH_SIZE = 900;
        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ids.size());
            List<Integer> chunk = ids.subList(i, end);
            userRepository.findAllById(chunk).forEach(u ->
                    result.put(u.getUserId(), u.getUserName())
            );
        }
        return result;
    }

    private UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setUserId(u.getUserId());
        d.setUserName(u.getUserName());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole());
        d.setCreatedAt(u.getCreatedAt());
        d.setHidden(u.isHidden());
        d.setGroupId(u.getGroupId());
        return d;
    }
}