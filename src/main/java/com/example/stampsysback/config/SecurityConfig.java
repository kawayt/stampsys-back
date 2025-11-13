package com.example.stampsysback.config;

import com.example.stampsysback.security.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    // 以下のパスは認証なしでアクセス可能
                    .requestMatchers("/", "/login**", "/error", "/actuator/**", "/api/test-create", "/api/stamp-send").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .loginPage("/oauth2/authorization/microsoft")
                    .userInfoEndpoint(userInfo -> userInfo
                            // OIDC の場合は oidcUserService を登録する
                            .oidcUserService(customOidcUserService)
                    )
                    .defaultSuccessUrl("/app", true)
            )
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}