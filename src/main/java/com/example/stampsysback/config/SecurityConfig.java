package com.example.stampsysback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    // SecurityFilterChain の設定
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        // ログイン成功後に常にフロントを表示するハンドラを作る
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        // フロントの URL に変更してください（例: http://localhost:5173/）
        successHandler.setDefaultTargetUrl("http://localhost:5173/");
        // 常に default を使う（保存されたリクエストを無視する）
        successHandler.setAlwaysUseDefaultTargetUrl(true);

        // OIDC logout handler (post logout redirect は別に設定している想定)
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:5173/logged-out");

        http
                // CORS を有効化（下で CorsFilter を登録しているため、ここは有効のままでOK）
                .cors()
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
        // CSRF はデフォルトで有効。fetch を使う場合はフロントでXSRFトークン送付が必要。
        ;

        return http.build();
    }

    // CORS 設定ソース（必要に応じてこちらを使う）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 明示的にフロントの origin を指定する（ワイルドカード不可 when allowCredentials=true）
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Location"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // グローバルな CorsFilter を登録して、プリフライトと実レスポンス両方で必ず正しいヘッダが返るようにする（開発環境向け）
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(CorsConfigurationSource corsConfigurationSource) {
        CorsFilter corsFilter = new CorsFilter((UrlBasedCorsConfigurationSource) corsConfigurationSource);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(corsFilter);
        // 高い優先度で実行（セキュリティフィルタの前に CORS ヘッダを追加）
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}