package com.example.stampsysback.config;

import com.example.stampsysback.security.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOidcUserService customOidcUserService;

    // application.properties / application.yml 側で設定可能にする
    @Value("${app.oauth2-registration-id:microsoft}")
    private String oauth2RegistrationId;

    @Value("${app.post-logout-redirect-uri:http://localhost:5173}")
    private String postLogoutRedirectUri;

    //CORS 用オリジン（カンマ区切りで複数指定可能）
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsProperty;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository,
                          CustomOidcUserService customOidcUserService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 認証不要なパス（必要最小限に限定）
                        .requestMatchers(
                                "/",
                                "/login**",
                                "/error",
                                "/actuator/**",
                                "/api/test-create",
                                "/api/stamp-send",
                                "/api/rooms/*/stamp-summary",
                                "/api/stamp-management/**",
                                "/api/rooms/*/stamp-activity",
                                "/static/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ユーザー一覧・編集は管理者・教員のみ（バックエンドで制御）
                        .requestMatchers("/users", "/users/**", "/api/users", "/api/users/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        // それ以外は認証要求
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // application 側の registration id と合わせてください
                        .loginPage("/oauth2/authorization/" + oauth2RegistrationId)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                        .defaultSuccessUrl("http://localhost:5173", true)
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(postLogoutRedirectUri);
        return handler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // allowedOriginsProperty はカンマ区切りで複数オリジンを指定できます
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 必要なヘッダを追加（X-XSRF-TOKEN などが使われる場合はここに追加）
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Requested-With", "*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}