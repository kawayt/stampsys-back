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
import org.springframework.http.HttpMethod;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOidcUserService customOidcUserService;

    @Value("${app.oauth2-registration-id:microsoft}")
    private String oauth2RegistrationId;

    @Value("${app.post-logout-redirect-uri:http://localhost:5173}")
    private String postLogoutRedirectUri;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsProperty;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

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
                        // --- 先に「より具体的な」パスを評価する（順序が重要） ---

                        // クラス一覧やクラス配下リソースは authenticated（STUDENT を含む）に許可
                        .requestMatchers(HttpMethod.GET, "/api/classes", "/api/classes/**").authenticated()

                        // 特定ユーザーの所属クラスはログイン済みなら誰でも取得できるようにする
                        .requestMatchers(HttpMethod.GET, "/api/users/*/classes").authenticated()

                        // 管理者／教員のみが参照できる API（ユーザー一覧 / 単一ユーザーの情報）
                        // NOTE: ここでは /api/users と /api/users/* のみを保護し、"/api/users/**" のような広域マッチは避ける
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER", "ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER", "ADMIN", "TEACHER")

                        // 管理者／教員のみが参照できるスタンプ一覧（エンドポイントが /api/stamps の場合）
                        .requestMatchers(HttpMethod.GET, "/api/stamps", "/api/stamps/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER", "ADMIN", "TEACHER")


                        // 管理者のみの更新／管理系
                        .requestMatchers("/api/users/hidden", "/api/users/hidden/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/role").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/hidden").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/api/admin/db/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/rooms/*/restore").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // フロントの /users ページ（静的）は ADMIN/TEACHER のみ
                        .requestMatchers("/users", "/users/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER", "ADMIN", "TEACHER")

                        // 認証不要（公開）パスは後ろにまとめる
                        .requestMatchers(
                                "/",
                                "/login**",
                                "/error",
                                "/actuator/**",
                                "/api/test-create",
                                "/api/stamp-send",
                                "/api/rooms/*/stamp-summary",
                                "/api/notes",
                                "/api/stamp-management/**",
                                "/api/rooms/*/close",
                                "/api/rooms/*/delete",
                                "/static/**",
                                "/favicon.ico",
                                "/api/rooms",
                                "/api/rooms/**",
                                "/setup",
                                "/setup/**",
                                "/api/setup/**"
                        ).permitAll()

                        // それ以外は認証要求
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/microsoft")
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                        .defaultSuccessUrl(frontendBaseUrl != null && !frontendBaseUrl.isBlank() ? frontendBaseUrl : "http://localhost:5173", true)
                        .failureHandler((request, response, exception) -> {
                            String redirect;
                            if (frontendBaseUrl != null && !frontendBaseUrl.isBlank()) {
                                redirect = frontendBaseUrl + "/login-disabled";
                            } else {
                                redirect = "/login-disabled";
                            }
                            response.sendRedirect(redirect);
                        })
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

    /**
     * Cross-SiteでのCookie利用（Vercel -> Render）を可能にするため、
     * CookieのSameSite属性をNoneに設定する。
     */
    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Requested-With", "*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}