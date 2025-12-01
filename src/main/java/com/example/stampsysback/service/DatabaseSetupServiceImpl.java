package com.example.stampsysback.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

/**
 * DatabaseSetupService の簡易実装（抜粋）。
 * - 既存プロジェクトに同名クラスがあれば、その initializeSchema() に admin_audit_logs の DDL を追加してください。
 */
@Service
public class DatabaseSetupServiceImpl implements DatabaseSetupService {

    private final JdbcTemplate jdbc;

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    public DatabaseSetupServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String buildCreateTableSql(CreateTableRequest req) {
        throw new UnsupportedOperationException("buildCreateTableSql is not implemented in this sample");
    }

    @Override
    @Transactional
    public void executeCreateTable(CreateTableRequest req, String sql) {
        if (sql == null || !sql.trim().toUpperCase().startsWith("CREATE TABLE")) {
            throw new IllegalArgumentException("不正な SQL が指定されました");
        }
        jdbc.execute(sql);
    }

    @Override
    public List<String> listPublicTables() {
        String q = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name";
        return jdbc.queryForList(q, String.class);
    }

    /**
     * initializeSchema: アプリ初期化で必要なシーケンス / テーブル / 関数 / トリガを作成する想定
     */
    @Override
    @Transactional
    public void initializeSchema() {
        List<String> statements = new ArrayList<>();

        // シーケンス
        statements.add("CREATE SEQUENCE IF NOT EXISTS users_user_id_seq");
        statements.add("CREATE SEQUENCE IF NOT EXISTS stamp_logs_stamp_log_id_seq");

        // 既存のテーブル作成 DDL（抜粋） — 必要に応じて他テーブルのDDLも追加してください
        statements.add(
                "CREATE TABLE IF NOT EXISTS public.classes (\n" +
                        "  class_id integer NOT NULL,\n" +
                        "  class_name varchar(255) NOT NULL,\n" +
                        "  created_at timestamptz NOT NULL,\n" +
                        "  deleted_at timestamptz,\n" +
                        "  CONSTRAINT classes_pkey PRIMARY KEY (class_id)\n" +
                        ");"
        );

        // （他のテーブルDDLをここに追加...）

        // admin_audit_logs を確実に作る（今回の問題の肝）
        statements.add(
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
                        ");"
        );

        // トリガ関数 / トリガ等（必要なら追加）
        // statements.add("CREATE OR REPLACE FUNCTION public.enforce_max_admins() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RETURN NEW; END; $$;");

        // 実行
        for (String s : statements) {
            String stmt = s == null ? "" : s.trim();
            if (stmt.isEmpty()) continue;
            jdbc.execute(stmt);
        }
    }
}