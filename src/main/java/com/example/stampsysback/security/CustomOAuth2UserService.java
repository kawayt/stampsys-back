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
import java.util.HashSet;
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
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        // 既存の処理: DB の user を作成/更新する等の処理を行う（省略せずそのまま保持してください）
        String providerUserId = attributes.containsKey("sub") ? String.valueOf(attributes.get("sub")) :
                (attributes.containsKey("oid") ? String.valueOf(attributes.get("oid")) : (attributes.containsKey("id") ? String.valueOf(attributes.get("id")) : null));

        // ...（既存の create/update 処理）

        // 最後に DB 上の role を参照して GrantedAuthority を作る
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 既存の権限

        try {
            User dbUser = null;
            if (providerUserId != null) {
                dbUser = userRepository.findByProviderUserId(providerUserId).orElse(null);
            }
            if (dbUser == null) {
                String email = attributes.getOrDefault("email", "").toString();
                if (!email.isBlank()) {
                    dbUser = userRepository.findByEmail(email).orElse(null);
                }
            }

            if (dbUser != null && "ADMIN".equals(dbUser.getRole())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        } catch (Exception ex) {
            logger.warn("Failed to load DB user for authority mapping", ex);
        }

        String nameAttributeKey = attributes.containsKey("sub") ? "sub" :
                (attributes.containsKey("oid") ? "oid" : (attributes.containsKey("id") ? "id" : "sub"));
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                authorities,
                attributes,
                nameAttributeKey
        );
    }
}