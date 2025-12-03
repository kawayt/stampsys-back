package com.example.stampsysback.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

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
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "アクセスが拒否されました"));
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