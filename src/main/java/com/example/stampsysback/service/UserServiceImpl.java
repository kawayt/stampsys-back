package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.dto.UserCountsDto;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        return listUsers(q, null);
    }

    // 新: role を受け取る listUsers（配列レスポンス用）
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
                            if (!u.getRole().equalsIgnoreCase(role)) return false;
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

    /**
     * ページネーション対応のユーザー一覧取得
     */
    @Override
    public Page<UserDto> listUsersPage(String q, int page, int size) {
        // 既存の引数互換のため、role = null として呼ぶ
        return listUsersPage(q, null, page, size);
    }

    // 新: role を考慮したページング実装
    @Override
    public Page<UserDto> listUsersPage(String q, String role, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());

        Page<User> pageResult;

        // ロール指定があるかどうかでリポジトリメソッドを使い分け
        if ((q == null || q.trim().isEmpty()) && (role == null || "ALL".equalsIgnoreCase(role))) {
            pageResult = userRepository.findByHiddenFalse(pageable);
        } else if ((q == null || q.trim().isEmpty()) && role != null && !"ALL".equalsIgnoreCase(role)) {
            // role 指定のみ：リポジトリに findByRoleAndHiddenFalse を追加して利用
            pageResult = userRepository.findByRoleAndHiddenFalse(role, pageable);
        } else {
            // q がある場合：repository の searchVisible があればそれを使うのがベスト（ここではフォールバック実装）
            // フォールバック: 全件検索 → フィルタ → page 化
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

    /**
     * 追加: 非表示ユーザーのページ取得（管理者向け）
     */
    @Override
    public Page<UserDto> listHiddenUsersPage(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("userId").ascending());
        Page<User> pageResult;
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
        User u = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        u.setRole(newRole);
        userRepository.save(u);
        return toDto(u);
    }

    @Override
    @Transactional
    public UserDto updateHidden(Integer userId, boolean hidden) {
        User u = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        u.setHidden(hidden);
        userRepository.save(u);
        return toDto(u);
    }

    @Override
    public UserCountsDto getUserCounts() {
        return getUserCounts(null);
    }

    // 新: role 指定でカウントを返す（role=null or ALL は従来どおり全体を返す）
    @Override
    public UserCountsDto getUserCounts(String role) {
        if (role != null && !"ALL".equalsIgnoreCase(role)) {
            long filtered = userRepository.countByRoleAndHiddenFalse(role);
            // 他の role カウントは 0 にして total に filtered をセットする（フロント側は total を使う想定）
            UserCountsDto dto = new UserCountsDto();
            if ("ADMIN".equalsIgnoreCase(role)) dto.setAdmin((int) filtered);
            else if ("TEACHER".equalsIgnoreCase(role)) dto.setTeacher((int) filtered);
            else if ("STUDENT".equalsIgnoreCase(role)) dto.setStudent((int) filtered);
            dto.setTotal((int) filtered);
            return dto;
        } else {
            // 既存の実装: 全体の管理者/教員/学生/total を返す
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
}