package com.example.stampsysback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
public class LogoutController {

    /**
     * フレンドリーなログアウト確認ページを返す。
     * ページは自動的にPOST /logout を submit してログアウトを行う（CSRF トークン付き）。
     */
    @GetMapping(value = "/logout-confirm", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logoutConfirm(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        String paramName = token.getParameterName();
        String tokenValue = token.getToken();

        String html = "<!doctype html>\n" +
                "<html>\n<head>\n<meta charset='utf-8'/>\n<title>Logging out...</title>\n</head>\n<body style='font-family:system-ui, sans-serif; text-align:center; padding-top:48px;'>\n" +
                "<h1>ログアウトしています…</h1>\n" +
                "<p>しばらくお待ちください。自動的に処理されます。</p>\n                " +
                "<form id='logoutForm' method='post' action='/logout'>\n" +
                "  <input type='hidden' name='" + paramName + "' value='" + tokenValue + "' />\n" +
                "  <noscript><button type='submit'>ログアウト</button></noscript>\n" +
                "</form>\n" +
                "<script>setTimeout(function(){document.getElementById('logoutForm').submit();}, 500);</script>\n" +
                "</body>\n</html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}