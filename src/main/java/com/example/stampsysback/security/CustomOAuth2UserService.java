package com.example.stampsysback.security;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        var ref = new Object() {
            String providerUserId = null;
        };
        if (attributes.containsKey("sub")) {
            ref.providerUserId = String.valueOf(attributes.get("sub"));
        } else if (attributes.containsKey("id")) {
            ref.providerUserId = String.valueOf(attributes.get("id"));
        } else if (attributes.containsKey("oid")) {
            ref.providerUserId = String.valueOf(attributes.get("oid"));
        }

        // email と name を確定（再代入しない）
        String emailTemp = (String) attributes.getOrDefault("email", attributes.get("userPrincipalName"));
        if (emailTemp == null && attributes.containsKey("upn")) {
            emailTemp = (String) attributes.get("upn");
        }
        final String email = emailTemp;

        final String name = (String) attributes.getOrDefault("name",
                attributes.getOrDefault("displayName", attributes.get("given_name")));

        if (ref.providerUserId != null) {
            try {
                userRepository.findByProviderUserId(ref.providerUserId).ifPresentOrElse(existing -> {
                    logger.info("Updating existing user: providerId={}, name={}, email={}", ref.providerUserId, name, email);
                    existing.setUserName(name != null ? name : existing.getUserName());
                    existing.setEmail(email != null ? email : existing.getEmail());
                    userRepository.save(existing);
                    logger.info("Updated user id={}", existing.getUserId());
                }, () -> {
                    logger.info("Creating new user: providerId={}, name={}, email={}", ref.providerUserId, name, email);
                    User user = new User();
                    // userId は DB 側の自動採番（IDENTITY）に任せる
                    user.setProviderUserId(ref.providerUserId);
                    user.setUserName(name != null ? name : "Unknown");
                    user.setEmail(email != null ? email : "unknown@example.com");
                    user.setRole("STUDENT");
                    user.setCreatedAt(OffsetDateTime.now());
                    User saved = userRepository.save(user);
                    logger.info("Created user id={}", saved.getUserId());
                });
            } catch (Exception ex) {
                logger.error("Failed to save user (providerId=" + ref.providerUserId + ")", ex);
            }
        } else {
            logger.warn("No provider user id found in attributes: {}", attributes.keySet());
        }

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub" // user name attribute のキー（環境によって "id" などに変更）
        );
    }
}