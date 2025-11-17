package com.example.stampsysback.config;

import com.example.stampsysback.security.CustomOidcUserService;
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
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
@EnableMethodSecurity // メソッドレベルの @PreAuthorize を使う場合に必要
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    public SecurityConfig(CustomOidcUserService customOidcUserService) {
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 認証不要なパス
                        .requestMatchers(HttpMethod.GET,"/", "/login**", "/error", "/users", "/users/**", "/api/users/**", "/users/admins/count", "/actuator/**", "/api/test-create", "/api/stamp-send", "/api/users", "/api/rooms/*/stamp-summary", "/api/rooms/*/stamp-activity").permitAll()

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
                        // 任意: サーバセッションを無効化
                        .invalidateHttpSession(true)
                        // 任意: SecurityContext をクリア
                        .clearAuthentication(true)
                        // 任意: JSESSIONID などの Cookie を削除
                        .deleteCookies("JSESSIONID")
                        // ログアウト成功時の挙動をカスタム
                        .logoutSuccessHandler(reactRedirectLogoutSuccessHandler())
                );

        return http.build();
    }

    /**
     * ログアウト完了後に React フロントのログイン画面へリダイレクトするハンドラ
     */
    @Bean
    public LogoutSuccessHandler reactRedirectLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            // 必要ならここで追加の Cookie を削除
            clearCookie("XSRF-TOKEN", request, response);
            clearCookie("X-XSRF-TOKEN", request, response);

            // React アプリのログインページまたはトップへリダイレクト
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", "http://localhost:5173/login");
        };
    }

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

        // 必要ならレスポンスヘッダの露出設定も可能
        // config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 全パスに上記設定を適用
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}