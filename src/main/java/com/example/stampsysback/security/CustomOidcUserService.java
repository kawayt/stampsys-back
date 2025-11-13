package com.example.stampsysback.security;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

/**
 * OIDC 用のカスタムユーザーサービス。
 * OIDC の認証フローで呼ばれ、DB への保存／更新処理をここで行う。
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // 標準処理で OidcUser を取得
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> claims = oidcUser.getClaims();

        logger.info("OIDC claims: {}", claims);

        // provider の一意キーを決める（sub -> oid -> id）
        String providerUserId;
        if (claims.containsKey("sub")) {
            providerUserId = String.valueOf(claims.get("sub"));
        } else if (claims.containsKey("oid")) {
            providerUserId = String.valueOf(claims.get("oid"));
        } else if (claims.containsKey("id")) {
            providerUserId = String.valueOf(claims.get("id"));
        } else {
            providerUserId = null;
        }
        logger.info("Determined providerUserId={}", providerUserId);

        String email = claims.containsKey("email") ? String.valueOf(claims.get("email")) : null;
        String name = claims.containsKey("name") ? String.valueOf(claims.get("name"))
                : (claims.containsKey("given_name") ? String.valueOf(claims.get("given_name")) : null);

        logger.info("Determined name={}, email={}", name, email);

        if (providerUserId != null) {
            try {
                userRepository.findByProviderUserId(providerUserId).ifPresentOrElse(existing -> {
                    logger.info("Existing user found (providerId={}), updating...", providerUserId);
                    existing.setUserName(name != null ? name : existing.getUserName());
                    existing.setEmail(email != null ? email : existing.getEmail());
                    userRepository.saveAndFlush(existing);
                    logger.info("Updated user id={}", existing.getUserId());
                }, () -> {
                    logger.info("No existing user for providerId={}, creating...", providerUserId);

                    // 最初のユーザーかどうか確認
                    boolean tableEmpty = userRepository.count() == 0L;
                    String assignedRole = tableEmpty ? "ADMIN" : "STUDENT";

                    User u = new User();
                    u.setProviderUserId(providerUserId);
                    u.setUserName(name != null ? name : "Unknown");
                    u.setEmail(email != null ? email : "unknown@example.com");
                    u.setRole(assignedRole);
                    u.setCreatedAt(OffsetDateTime.now());

                    try {
                        User saved = userRepository.saveAndFlush(u);
                        logger.info("Created user id={} role={}", saved.getUserId(), saved.getRole());
                    } catch (DataIntegrityViolationException dive) {
                        logger.warn("Data integrity violation when creating user with assignedRole={}. Will fallback to STUDENT. cause={}",
                                assignedRole, dive.getMessage());
                        if ("ADMIN".equals(assignedRole)) {
                            u.setRole("STUDENT");
                            User savedFallback = userRepository.saveAndFlush(u);
                            logger.info("Created user id={} role={} (fallback)", savedFallback.getUserId(), savedFallback.getRole());
                        } else {
                            throw dive;
                        }
                    }
                });
            } catch (Exception ex) {
                logger.error("Failed to save/update user for providerId=" + providerUserId, ex);
            }
        } else {
            logger.warn("No provider user id found in OIDC claims: {}", claims.keySet());
        }

        // 元の OidcUser をそのまま返す（または必要に応じて権限を加工して返す）
        return oidcUser;
    }
}