package com.example.stampsysback.config;

import com.example.stampsysback.security.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;

/**
 * Security 設定
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    // post-logout の戻り先。Azure のアプリ登録で事前に許可しておくこと。
    @Value("${ms.post_logout_redirect_uri:http://localhost:5173/login}")
    private String postLogoutRedirectUri;

    public SecurityConfig(CustomOidcUserService customOidcUserService,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.customOidcUserService = customOidcUserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 認証不要なパス
                        .requestMatchers(
                                "/",
                                "/login**",
                                "/error",
                                "/users",
                                "/users/**",
                                "/api/users/**",
                                "/users/admins/count",
                                "/actuator/**",
                                "/api/test-create",
                                "/api/stamp-send",
                                "/api/users",
                                "/api/rooms/*/stamp-summary",
                                "/api/stamp-management/**",
                                "/api/rooms/*/stamp-activity"
                        ).permitAll()
                        // それ以外は認証を要求
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/microsoft")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .defaultSuccessUrl("http://localhost:5173", true)
                )
                .logout(logout -> logout
                        // サーバセッションを無効化
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        // JSESSIONID 等を削除
                        .deleteCookies("JSESSIONID")
                        // ログアウト成功時は OIDC の end_session_endpoint へ遷移させる
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    /**
     * OIDC の end_session_endpoint に遷移するハンドラ
     * ClientRegistrationRepository を使って provider の end_session_endpoint を解決し、
     * post_logout_redirect_uri を付与してリダイレクトします。
     */
    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // post logout の戻り先（テンプレートや固定 URL を設定）
        // 例: "http://localhost:5173/login"（Azure のアプリ登録で許可しておく）
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(postLogoutRedirectUri);

        // 必要ならセッションや Cookie を追加でクリアする処理をここに差し込めますが、
        // filterChain の logout() 設定で invalidateHttpSession/clearAuthentication/deleteCookies は行われます。
        return oidcLogoutSuccessHandler;
    }

    // (オプション) 以前のクッキークリアユーティリティを残しておく
    private void clearCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() == null) return;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) {
                Cookie cookie = new Cookie(name, "");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setHttpOnly(c.isHttpOnly());
                cookie.setSecure(c.getSecure());
                response.addCookie(cookie);
            }
        }
    }

    // CORS 設定
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // フロントエンドのオリジンを許可
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // 認証付きリクエストを許可
        config.setAllowCredentials(true);

        // 許可するメソッド
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 許可するヘッダ
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 全パスに上記設定を適用
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}