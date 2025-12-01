package com.example.stampsysback.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 管理者によるDB直接操作の監査ログを記録するサービス。
 *
 * - admin_audit_logs テーブルが存在しない場合はテーブル作成を試みる（フォールバック）。
 * - それでも失敗した場合、例外は握りつぶしてログ出力に留める（監査漏れは稀に起き得るが、
 *   画面操作の妨げにならないようにするため）。
 */
@Service
public class AdminAuditLogService {

    private final JdbcTemplate jdbcTemplate;

    public AdminAuditLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 監査ログを1件挿入する。
     *
     * @param adminUserId  操作した管理者ユーザーのID（users.user_id）
     * @param adminEmail   管理者のメールアドレス
     * @param tableName    操作したテーブル名（publicスキーマ）
     * @param operation    操作種別（"SELECT" / "INSERT" / "UPDATE" / "DELETE"）
     * @param primaryKeys  主キー情報（文字列化）
     * @param beforeData   操作前のデータ（JSON文字列, null可）
     * @param afterData    操作後のデータ（JSON文字列, null可）
     */
    public void log(Integer adminUserId,
                    String adminEmail,
                    String tableName,
                    String operation,
                    String primaryKeys,
                    String beforeData,
                    String afterData) {
        // SQL を組み立てる（NULL の場合は NULL を直接書く）
        String sql = "INSERT INTO public.admin_audit_logs " +
                "(admin_user_id, admin_email, table_name, operation, primary_keys, before_data, after_data) " +
                "VALUES (?, ?, ?, ?, ?, " +
                (beforeData != null ? "?::jsonb" : "NULL") + ", " +
                (afterData != null ? "?::jsonb" : "NULL") +
                ")";

        try {
            // try insert
            if (beforeData != null && afterData != null) {
                jdbcTemplate.update(sql,
                        adminUserId,
                        adminEmail,
                        tableName,
                        operation,
                        primaryKeys,
                        beforeData,
                        afterData
                );
            } else if (beforeData != null) {
                jdbcTemplate.update(sql,
                        adminUserId,
                        adminEmail,
                        tableName,
                        operation,
                        primaryKeys,
                        beforeData
                );
            } else if (afterData != null) {
                jdbcTemplate.update(sql,
                        adminUserId,
                        adminEmail,
                        tableName,
                        operation,
                        primaryKeys,
                        afterData
                );
            } else {
                jdbcTemplate.update(sql,
                        adminUserId,
                        adminEmail,
                        tableName,
                        operation,
                        primaryKeys
                );
            }
            return;
        } catch (DataAccessException dae) {
            // ここで最も多いのは "relation \"public.admin_audit_logs\" does not exist"
            Throwable root = dae.getMostSpecificCause();
            String msg = root == null ? dae.getMessage() : root.getMessage();
            // 判定: テーブルが存在しないケースを検出して DDL を発行してリトライ
            if (msg != null && msg.toLowerCase().contains("does not exist")
                    || msg != null && msg.toLowerCase().contains("relation \"admin_audit_logs\"")) {
                try {
                    createAuditTableIfNotExists();
                    // 再試行（1回のみ）
                    if (beforeData != null && afterData != null) {
                        jdbcTemplate.update(sql,
                                adminUserId,
                                adminEmail,
                                tableName,
                                operation,
                                primaryKeys,
                                beforeData,
                                afterData
                        );
                    } else if (beforeData != null) {
                        jdbcTemplate.update(sql,
                                adminUserId,
                                adminEmail,
                                tableName,
                                operation,
                                primaryKeys,
                                beforeData
                        );
                    } else if (afterData != null) {
                        jdbcTemplate.update(sql,
                                adminUserId,
                                adminEmail,
                                tableName,
                                operation,
                                primaryKeys,
                                afterData
                        );
                    } else {
                        jdbcTemplate.update(sql,
                                adminUserId,
                                adminEmail,
                                tableName,
                                operation,
                                primaryKeys
                        );
                    }
                    return;
                } catch (Exception retryEx) {
                    // テーブル作成や再試行にも失敗したら、ログに残して握りつぶす
                    System.err.println("Failed to create admin_audit_logs or insert audit after retry:");
                    retryEx.printStackTrace();
                    return;
                }
            }
            // それ以外の DataAccessException はログに残して握りつぶす（監査ログは補助的）
            System.err.println("Failed to insert admin audit log: " + msg);
            dae.printStackTrace();
        } catch (Exception ex) {
            // その他例外はログに出すだけ
            System.err.println("Unexpected error while logging admin audit:");
            ex.printStackTrace();
        }
    }

    /**
     * admin_audit_logs テーブルを作成する（IF NOT EXISTS を使う）。
     * - 単純な DDL を発行し、存在しなければ作る。
     * - マイグレーションがあるならそちらを優先してください（ここはフォールバック）。
     */
    private void createAuditTableIfNotExists() {
        String ddl =
                "CREATE TABLE IF NOT EXISTS public.admin_audit_logs (\n" +
                        "  audit_id      bigserial PRIMARY KEY,\n" +
                        "  admin_user_id integer NOT NULL,\n" +
                        "  admin_email   varchar(255),\n" +
                        "  table_name    varchar(255) NOT NULL,\n" +
                        "  operation     varchar(10)  NOT NULL,\n" +
                        "  primary_keys  text,\n" +
                        "  before_data   jsonb,\n" +
                        "  after_data    jsonb,\n" +
                        "  executed_at   timestamptz  NOT NULL DEFAULT now()\n" +
                        ");";
        jdbcTemplate.execute(ddl);
    }
}