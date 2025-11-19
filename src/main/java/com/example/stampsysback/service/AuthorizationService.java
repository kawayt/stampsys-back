package com.example.stampsysback.service;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import com.example.stampsysback.repository.UsersClassesRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final UsersClassesRepository usersClassesRepository;
    private final UserRepository userRepository;

    public AuthorizationService(UsersClassesRepository usersClassesRepository, UserRepository userRepository) {
        this.usersClassesRepository = usersClassesRepository;
        this.userRepository = userRepository;
    }

    /**
     * principal から DB の userId を解くユーティリティ。
     * AppController にあるロジックと同様の方法で providerUserId や email を用いて DB からユーザを探す。
     * 戻り値は user.userId (Integer) か、見つからなければ null。
     */
    public Integer resolveCurrentUserId(OAuth2User principal) {
        if (principal == null) return null;
        Object subObj = principal.getAttribute("sub");
        Object oidObj = principal.getAttribute("oid");
        Object idObj = principal.getAttribute("id");
        String providerId = firstNonNullString(subObj, oidObj, idObj);

        User dbUser = null;
        if (providerId != null) {
            dbUser = userRepository.findByProviderUserId(providerId).orElse(null);
        }
        if (dbUser == null) {
            // principal.getAttribute("email") を複数回呼ばないように局所変数に格納してから null チェックすることで
            // 静的解析の「null の可能性」警告を解消します。
            Object emailObj = principal.getAttribute("email");
            String email = emailObj != null ? String.valueOf(emailObj) : null;
            if (email != null) {
                dbUser = userRepository.findByEmail(email).orElse(null);
            }
        }
        return dbUser != null ? dbUser.getUserId() : null;
    }

    public boolean isUserInClass(Integer userId, Integer classId) {
        if (userId == null || classId == null) return false;
        try {
            return usersClassesRepository.existsByUserIdAndClassId(userId, classId);
        } catch (Exception ex) {
            // safe default
            return false;
        }
    }

    public boolean isTeacherOrAdmin(Integer userId) {
        if (userId == null) return false;
        return userRepository.findById(userId).map(u -> {
            String role = u.getRole();
            return "ADMIN".equals(role) || "TEACHER".equals(role);
        }).orElse(false);
    }

    private static String firstNonNullString(Object... objs) {
        for (Object o : objs) {
            if (o != null) return String.valueOf(o);
        }
        return null;
    }
}