package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.dto.UserDto;
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

    // 管理者ロールの上限（DB側制約と合わせる）
    private static final int MAX_ADMIN = 5;

    // ---------- 一覧（非ページング） ----------
    @Override
    public List<UserDto> listUsers(String q) {
        return listUsers(q, null);
    }

    public List<UserDto> listUsers(String q, String role) {
        String keyword = (q == null || q.trim().isEmpty()) ? null : q.toLowerCase();
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.isHidden())
                .filter(u -> {
                    if (role != null && !"ALL".equalsIgnoreCase(role)) {
                        return u.getRole() != null && u.getRole().equalsIgnoreCase(role);
                    }
                    return true;
                })
                .filter(u -> {
                    if (keyword == null) return true;
                    return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                })
                .collect(Collectors.toList());
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ---------- ページング（検索のみ） ----------
    @Override
    public Page<UserDto> listUsersPage(String q, int page, int size) {
        return listUsersPage(q, null, null, page, size);
    }

    // ---------- ページング（検索 + ロール） ----------
    @Override
    public Page<UserDto> listUsersPage(String q, String role, int page, int size) {
        return listUsersPage(q, role, null, page, size);
    }

    // ---------- ページング（検索 + ロール + 所属） ----------
    @Override
    public Page<UserDto> listUsersPage(String q, String role, Integer groupId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by("userId").ascending() // 並びを固定
        );
        String keyword = (q == null || q.trim().isEmpty()) ? null : q.trim();
        Page<User> pageResult = userRepository.searchVisible(keyword, role, groupId, pageable);
        return pageResult.map(this::toDto);
    }

    // ---------- 非表示ユーザー一覧 ----------
    @Override
    public Page<UserDto> listHiddenUsersPage(String q, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by("userId").ascending()
        );
        String keyword = (q == null || q.trim().isEmpty()) ? null : q.toLowerCase();

        List<User> hiddenUsers = userRepository.findByHiddenTrue().stream()
                .filter(u -> {
                    if (keyword == null) return true;
                    return (u.getUserName() != null && u.getUserName().toLowerCase().contains(keyword))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword));
                })
                .toList();

        int start = Math.min(hiddenUsers.size(), (int) pageable.getOffset());
        int end = Math.min(hiddenUsers.size(), start + pageable.getPageSize());
        List<User> content = hiddenUsers.subList(start, end);

        return new PageImpl<>(content.stream().map(this::toDto).toList(), pageable, hiddenUsers.size());
    }

    // ---------- ロール更新 ----------
    @Override
    @Transactional
    public UserDto updateRole(Integer userId, String newRole) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        String prevRole = u.getRole();

        // 現在の役割がADMINで新役割がADMINでない場合、確認
        if ("ADMIN".equalsIgnoreCase(prevRole) && !"ADMIN".equalsIgnoreCase(newRole)) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (adminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理者の人数は最低1人必要です。最後の表示中の管理者を削除または降格できません。");
            }
        }

        // 新しい役割がADMINの場合、管理者数が MAX_ADMIN 人を越えないように確認
        if ("ADMIN".equalsIgnoreCase(newRole)) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (!"ADMIN".equalsIgnoreCase(prevRole) && adminCount >= MAX_ADMIN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "管理者権限は最大" + MAX_ADMIN + "人まで設定可能です。"
                );
            }
        }
        u.setRole(newRole);
        userRepository.save(u);
        return toDto(u);
    }

    // ---------- 非表示更新 ----------
    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        // 非表示かつ現在の役割が ADMIN の場合、チェックを追加
        if (hidden && "ADMIN".equalsIgnoreCase(u.getRole())) {
            long adminCount = userRepository.countByRoleAndHiddenFalse("ADMIN");
            if (adminCount <= 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理者の人数は最低1人必要です。最後の表示中の管理者を非表示にはできません。");
            }
        }

        u.setHidden(hidden);
        userRepository.save(u);
        return toDto(u);
    }

    // ---------- ユーザー数カウント ----------
    @Override
    public UserCountsDto getUserCounts() {
        return getUserCounts(null);
    }

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

    // ---------- グループ更新 ----------
    @Override
    @Transactional
    public UserDto updateGroup(Integer userId, Integer groupId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
        u.setGroupId(groupId);
        userRepository.save(u);
        return toDto(u);
    }

    // ---------- ID集合から名前取得 ----------
    @Override
    public Map<Integer, String> getUserNamesByIds(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<Integer> ids = userIds.stream().filter(Objects::nonNull).distinct().toList();
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

    // ---------- DTO 変換 ----------
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

    @Override
    public Map<Integer, Long> getUserCountsByGroup() {
        List<Object[]> results = userRepository.countUsersByGroup();
        Map<Integer, Long> counts = new HashMap<>();

        for (Object[] result : results) {
            Integer groupId = (Integer) result[0];
            Long count = (Long) result[1];
            counts.put(groupId, count);
        }
        return counts;
    }
}