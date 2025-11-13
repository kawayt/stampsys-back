package com.example.stampsysback.security;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // まず標準処理で取得
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        // デバッグログ：属性全体を出す（ログに残すことで実際にどのキーが来ているか確認）
        logger.info("OAuth2 attributes: {}", attributes);

        // provider の一意キー候補を決定（sub -> oid -> id）
        String providerUserId;
        if (attributes.containsKey("sub")) {
            providerUserId = String.valueOf(attributes.get("sub"));
        } else if (attributes.containsKey("oid")) {
            providerUserId = String.valueOf(attributes.get("oid"));
        } else if (attributes.containsKey("id")) {
            providerUserId = String.valueOf(attributes.get("id"));
        } else {
            providerUserId = null;
        }
        logger.info("Determined providerUserId={}", providerUserId);

        // email と name（再代入しない形で確定）
        String emailTemp = (String) attributes.getOrDefault("email", attributes.getOrDefault("userPrincipalName", null));
        if (emailTemp == null && attributes.containsKey("upn")) {
            emailTemp = String.valueOf(attributes.get("upn"));
        }
        final String email = emailTemp;
        final String name = (String) attributes.getOrDefault("name",
                attributes.getOrDefault("displayName", attributes.get("given_name")));

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
                    logger.info("No existing user found for providerId={}, creating new user...", providerUserId);

                    // ここで最初のユーザーかどうかを判定して role を決定する
                    boolean tableEmpty = userRepository.count() == 0L;
                    String assignedRole = tableEmpty ? "ADMIN" : "STUDENT";

                    User user = new User();
                    user.setProviderUserId(providerUserId);
                    user.setUserName(name != null ? name : "Unknown");
                    user.setEmail(email != null ? email : "unknown@example.com");
                    user.setRole(assignedRole);
                    user.setCreatedAt(OffsetDateTime.now());

                    try {
                        // INSERT を試みる
                        User saved = userRepository.saveAndFlush(user);
                        logger.info("Created user id={} role={}", saved.getUserId(), saved.getRole());
                    } catch (DataIntegrityViolationException dive) {
                        // 競合（同時に2人が空と判断して ADMIN を作ろうとした等）を想定してフォールバック
                        logger.warn("Data integrity violation when creating user with assignedRole={}. Will fallback to STUDENT. cause={}",
                                assignedRole, dive.getMessage());
                        if ("ADMIN".equals(assignedRole)) {
                            user.setRole("STUDENT");
                            User savedFallback = userRepository.saveAndFlush(user);
                            logger.info("Created user id={} role={} (fallback)", savedFallback.getUserId(), savedFallback.getRole());
                        } else {
                            throw dive; // role が STUDENT で弾かれるなら再スロー
                        }
                    }
                });
            } catch (Exception ex) {
                // 例外は必ずログを残す
                logger.error("Failed to save/update user for providerId=" + providerUserId, ex);
            }
        } else {
            logger.warn("No provider user id found in attributes: {}", attributes.keySet());
        }

        // 最後に DefaultOAuth2User を返す（nameAttributeKey は実際のキーに合わせる）
        String nameAttributeKey = attributes.containsKey("sub") ? "sub" :
                (attributes.containsKey("oid") ? "oid" : (attributes.containsKey("id") ? "id" : "sub"));
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttributeKey
        );
    }
}