package com.example.stampsysback.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * API 例外ハンドラ: 例外をわかりやすい HTTP ステータス + JSON メッセージで返す
 */
@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 追加: ResponseStatusException を優先的に処理する（サービス層で throw したメッセージを保持）
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        // ex はログ用途などで将来使う可能性があるため、現時点では参照しない
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました"));
    }

    private static String normalizeDbMessage(String raw) {
        if (raw == null) return null;

        // PostgreSQLのRAISE/例外は改行や「場所: ...」などが付くので、まずは先頭行を優先
        String firstLine = raw.split("\\R", 2)[0];
        String msg = firstLine != null ? firstLine.trim() : raw.trim();

        // 先頭に "ERROR:" が付くパターンを除去
        if (msg.startsWith("ERROR:")) {
            msg = msg.substring("ERROR:".length()).trim();
        }

        // DBトリガの管理者上限制約（メッセージゆれに対応）
        // 例: "Cannot have more than 5 ADMIN users" / "Cannot have more than 5 admin users"
        String upper = msg.toUpperCase(Locale.ROOT);
        if (upper.contains("CANNOT HAVE MORE THAN") && upper.contains("ADMIN") && upper.contains("USER")) {
            return "管理者権限は最大5人まで付与できます。";
        }

        return msg;
    }

    /**
     * DB制約違反（ユニーク制約/チェック制約/トリガ等）を 4xx として返す。
     * PSQLException が原因のことが多いが、Spring 側では DataIntegrityViolationException にラップされる。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // getMostSpecificCause() は null の場合があるため安全に取り出す
        Throwable mostSpecific = ex.getMostSpecificCause();
        String raw = mostSpecific != null ? mostSpecific.getMessage() : ex.getMessage();
        String normalized = normalizeDbMessage(raw);
        String safeMsg = (normalized == null || normalized.isBlank()) ? "データの整合性エラーが発生しました" : normalized;

        // 管理者上限など「状態の衝突」に寄せたいものは 409、それ以外は 400 を基本にする
        HttpStatus status = safeMsg.contains("管理者") && safeMsg.contains("最大")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(Map.of("message", safeMsg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        // 内部エラーは詳細を返さずログに残す
        String errorId = UUID.randomUUID().toString();
        logger.error("Unhandled exception (errorId={}): {}", errorId, ex.getMessage(), ex);

        // まず cause チェーンをたどって ResponseStatusException があればそれを使う
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ResponseStatusException rse) {
                String message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
                return ResponseEntity.status(rse.getStatusCode()).body(Map.of("message", message));
            }

            // 追加: PSQLException 等のDB例外が原因の場合も 4xx に寄せる（ネットワークエラー扱いにしない）
            String className = cause.getClass().getName();
            if (className.equals("org.postgresql.util.PSQLException")) {
                String normalized = normalizeDbMessage(cause.getMessage());
                String message = (normalized == null || normalized.isBlank())
                        ? "データベース制約違反が発生しました"
                        : normalized;

                HttpStatus status = message.contains("管理者") && message.contains("最大")
                        ? HttpStatus.CONFLICT
                        : HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(status).body(Map.of("message", message));
            }

            cause = cause.getCause();
        }

        // クライアントには汎用的なメッセージとエラーIDのみ返す（詳細はサーバログで確認）
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message", "サーバー側で予期しないエラーが発生しました。運用担当にお問い合わせください。",
                        "errorId", errorId
                ));
    }
}