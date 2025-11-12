package com.example.stampsysback.config;

import com.example.stampsysback.security.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//              .csrf(csrf -> csrf.disable())//元々の設定ではcsrfを無効にしていた
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login**", "/error", "/actuator/**", "/api/test-create").permitAll()
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