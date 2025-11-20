package com.example.stampsysback.service;

import com.example.stampsysback.mapper.UsersClassesMapper;
import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorizationService - MyBatis (UsersClassesMapper) に切り替えた版
 * 変更点:
 * - UsersClassesMapper を注入して exists チェックを行うようにしました（JdbcTemplate 実装からの切替）。
 * - UserRepository は引き続き使用して principal から DB user を解く resolveCurrentUserId と
 *   isTeacherOrAdmin を提供します。
 * - ログ出力を追加して障害時に原因が追いやすくしています。
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final UsersClassesMapper usersClassesMapper;
    private final UserRepository userRepository;

    /**
     * principal から DB の userId を解くユーティリティ。
     * provider 側の識別子（sub/oid/id）を優先して DB の user を探し、
     * 見つからなければ email で検索します。見つからなければ null を返す。
     */
    public Integer resolveCurrentUserId(OAuth2User principal) {
        if (principal == null) return null;
        Object subObj = principal.getAttribute("sub");
        Object oidObj = principal.getAttribute("oid");
        Object idObj = principal.getAttribute("id");
        String providerId = firstNonNullString(subObj, oidObj, idObj);

        User dbUser = null;
        if (providerId != null) {
            try {
                dbUser = userRepository.findByProviderUserId(providerId).orElse(null);
            } catch (Exception ex) {
                log.warn("Failed to lookup user by providerUserId={}", providerId, ex);
            }
        }
        if (dbUser == null) {
            Object emailObj = principal.getAttribute("email");
            String email = emailObj != null ? String.valueOf(emailObj) : null;
            if (email != null) {
                try {
                    dbUser = userRepository.findByEmail(email).orElse(null);
                } catch (Exception ex) {
                    log.warn("Failed to lookup user by email={}", email, ex);
                }
            }
        }
        return dbUser != null ? dbUser.getUserId() : null;
    }

    /**
     * 指定 userId が指定 classId に所属しているか
     * MyBatis マッパー側で SELECT EXISTS(...) を実行する実装を想定しています。
     */
    public boolean isUserInClass(Integer userId, Integer classId) {
        if (userId == null || classId == null) return false;
        try {
            Boolean exists = usersClassesMapper.existsByUserIdAndClassId(userId, classId);
            return Boolean.TRUE.equals(exists);
        } catch (Exception ex) {
            log.error("users_classes check failed for userId={}, classId={}", userId, classId, ex);
            return false;
        }
    }

    /**
     * ユーザーが TEACHER / ADMIN 権限を持つか確認
     */
    public boolean isTeacherOrAdmin(Integer userId) {
        if (userId == null) return false;
        try {
            return userRepository.findById(userId).map(u -> {
                String role = u.getRole();
                return "ADMIN".equals(role) || "TEACHER".equals(role);
            }).orElse(false);
        } catch (Exception ex) {
            log.error("Failed to check role for userId={}", userId, ex);
            return false;
        }
    }

    private static String firstNonNullString(Object... objs) {
        for (Object o : objs) {
            if (o != null) return String.valueOf(o);
        }
        return null;
    }
}