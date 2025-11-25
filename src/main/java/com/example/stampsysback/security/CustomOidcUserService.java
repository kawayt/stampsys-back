package com.example.stampsysback.security;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OIDC 用のカスタムユーザーサービス。
 * OIDC の認証フローで呼ばれ、DB への保存／更新処理および GrantedAuthority の付与を行う。
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

        // DB の create/update 処理（既存ロジックを維持）
        try {
            final String finalProviderUserId = providerUserId;
            if (providerUserId != null) {
                userRepository.findByProviderUserId(providerUserId).ifPresentOrElse(existing -> {
                    logger.info("Existing user found (providerId={}), updating...", finalProviderUserId);
                    existing.setUserName(name != null ? name : existing.getUserName());
                    existing.setEmail(email != null ? email : existing.getEmail());
                    userRepository.saveAndFlush(existing);
                    logger.info("Updated user id={}", existing.getUserId());
                }, () -> {
                    logger.info("No existing user for providerId={}, creating...", finalProviderUserId);

                    // 最初のユーザーかどうか確認
                    boolean tableEmpty = userRepository.count() == 0L;
                    String assignedRole = tableEmpty ? "ADMIN" : "STUDENT";

                    User u = new User();
                    u.setProviderUserId(finalProviderUserId);
                    u.setUserName(name != null ? name : "Unknown");
                    u.setEmail(email != null ? email : "unknown@example.com");
                    u.setRole(assignedRole);
                    u.setCreatedAt(OffsetDateTime.now());

                    try {
                        userRepository.saveAndFlush(u);
                        logger.info("Created new user id={}", u.getUserId());
                    } catch (DataIntegrityViolationException ex) {
                        logger.warn("Failed to create user (possible duplicate), ignoring", ex);
                    }
                });
            }
        } catch (Exception ex) {
            logger.warn("Failed to create/update DB user from OIDC claims", ex);
        }

        // 最後に DB 上の role を参照して GrantedAuthority を作成する（ここが重要）
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // デフォルトロール

        try {
            com.example.stampsysback.model.User dbUser = null;
            if (providerUserId != null) {
                dbUser = userRepository.findByProviderUserId(providerUserId).orElse(null);
            }
            if (dbUser == null && email != null && !email.isBlank()) {
                dbUser = userRepository.findByEmail(email).orElse(null);
            }

            // ここを汎用化：DB に role があれば ROLE_<ROLE> を付与する
            if (dbUser != null && dbUser.getRole() != null && !dbUser.getRole().isBlank()) {
                String roleName = dbUser.getRole().toUpperCase().trim();
                // 例: "TEACHER" -> "ROLE_TEACHER"
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            }
        } catch (Exception ex) {
            logger.warn("Failed to load DB user for authority mapping", ex);
        }

        // nameAttributeKey を決める（sub/oid/id の優先）
        String nameAttributeKey = claims.containsKey("sub") ? "sub" :
                (claims.containsKey("oid") ? "oid" : (claims.containsKey("id") ? "id" : "sub"));

        // DefaultOidcUser を返し、SecurityContext に上で作った authorities が反映されるようにする
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), nameAttributeKey);
    }
}