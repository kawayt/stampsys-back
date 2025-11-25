package com.example.stampsysback.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DatabaseSetupService の実装。
 * 既存の buildCreateTableSql / executeCreateTable / listPublicTables を備えつつ、
 * initializeSchema() を追加してアプリ用テーブル群を初期化します。
 */
@Service
public class DatabaseSetupServiceImpl implements DatabaseSetupService {

    private final JdbcTemplate jdbc;

    // 簡易的な識別子の正当性チェック（英字/アンダースコアから始まり英数字/アンダースコア）
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    // 許可する基本型（大文字で扱う）
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "INTEGER", "INT", "BIGINT", "TEXT", "VARCHAR", "BOOLEAN", "TIMESTAMP",
            "SERIAL", "BIGSERIAL"
    );

    public DatabaseSetupServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String buildCreateTableSql(CreateTableRequest req) {
        // 既に作成済み（省略）。もし同じファイルを使用している場合、こちらを維持してください。
        throw new UnsupportedOperationException("buildCreateTableSql is not implemented in this trimmed sample");
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
     * 初期スキーマを実行する（管理者のみが呼べる）
     * 実行順序に注意して、必要な SEQUENCE / FUNCTION を先に作成します。
     */
    @Override
    @Transactional
    public void initializeSchema() {
        // 1) 必要なシーケンス（存在しない場合）
        List<String> statements = new ArrayList<>();

        statements.add("CREATE SEQUENCE IF NOT EXISTS users_user_id_seq");
        statements.add("CREATE SEQUENCE IF NOT EXISTS stamp_logs_stamp_log_id_seq");
        // (必要なら別シーケンスを追加)

        // 2) trigger function: enforce_max_admins の簡易実装（no-op）
        statements.add(
                "CREATE OR REPLACE FUNCTION public.enforce_max_admins() RETURNS trigger LANGUAGE plpgsql AS $$\n" +
                        "BEGIN\n" +
                        "  -- 本番ではここに管理者数チェック等のロジックを入れてください\n" +
                        "  RETURN NEW;\n" +
                        "END;\n" +
                        "$$;"
        );

        // 3) テーブル作成（元の DDL を簡素化して使用）
        statements.add(
                "CREATE TABLE IF NOT EXISTS public.classes (\n" +
                        "  class_id integer NOT NULL,\n" +
                        "  class_name varchar(255) NOT NULL,\n" +
                        "  created_at timestamptz NOT NULL,\n" +
                        "  CONSTRAINT classes_pkey PRIMARY KEY (class_id)\n" +
                        ");"
        );

        statements.add(
                "CREATE TABLE IF NOT EXISTS public.rooms (\n" +
                        "  room_id integer NOT NULL,\n" +
                        "  room_name varchar(255) NOT NULL,\n" +
                        "  class_id integer NOT NULL,\n" +
                        "  active boolean NOT NULL,\n" +
                        "  created_at timestamptz NOT NULL,\n" +
                        "  hidden boolean NOT NULL DEFAULT false,\n" +
                        "  CONSTRAINT rooms_pkey PRIMARY KEY (room_id)\n" +
                        ");"
        );

        statements.add(
                "CREATE TABLE IF NOT EXISTS public.stamp_logs (\n" +
                        "  user_id integer NOT NULL,\n" +
                        "  room_id integer NOT NULL,\n" +
                        "  stamp_id integer NOT NULL,\n" +
                        "  sent_at timestamptz NOT NULL DEFAULT now(),\n" +
                        "  stamp_log_id integer NOT NULL DEFAULT nextval('stamp_logs_stamp_log_id_seq'::regclass),\n" +
                        "  CONSTRAINT stamp_logs_pkey PRIMARY KEY (stamp_log_id)\n" +
                        ");"
        );

        // NOTE: この CREATE の DEFAULT 次第で既存のシーケンス名に依存します（元 SQL では users_user_id_seq を参照している箇所がありました）。
        statements.add(
                "CREATE TABLE IF NOT EXISTS public.stamps (\n" +
                        "  stamp_id integer NOT NULL DEFAULT nextval('users_user_id_seq'::regclass),\n" + // 元のスクリプトに合わせる
                        "  stamp_name varchar(255) NOT NULL,\n" +
                        "  stamp_color integer NOT NULL,\n" +
                        "  stamp_icon integer NOT NULL,\n" +
                        "  stamp_deleted boolean NOT NULL DEFAULT false,\n" +
                        "  CONSTRAINT stamps_pkey PRIMARY KEY (stamp_id)\n" +
                        ");"
        );

        statements.add(
                "CREATE TABLE IF NOT EXISTS public.stamps_classes (\n" +
                        "  stamp_id integer NOT NULL,\n" +
                        "  class_id integer NOT NULL,\n" +
                        "  CONSTRAINT stamps_classes_pkey PRIMARY KEY (stamp_id, class_id)\n" +
                        ");"
        );

        statements.add(
                "CREATE TABLE IF NOT EXISTS public.users (\n" +
                        "  user_name varchar(255) NOT NULL,\n" +
                        "  provider_user_id varchar(255) NOT NULL,\n" +
                        "  email varchar(255) NOT NULL,\n" +
                        "  role varchar(255) NOT NULL,\n" +
                        "  created_at timestamptz NOT NULL,\n" +
                        "  user_id integer NOT NULL DEFAULT nextval('users_user_id_seq'::regclass),\n" +
                        "  hidden boolean NOT NULL DEFAULT false,\n" +
                        "  CONSTRAINT users_pkey PRIMARY KEY (user_id),\n" +
                        "  CONSTRAINT uk_users_email UNIQUE (email),\n" +
                        "  CONSTRAINT uk_users_provider_user_id UNIQUE (provider_user_id),\n" +
                        "  CONSTRAINT users_role_check CHECK (role = ANY (ARRAY['ADMIN','STUDENT','TEACHER']))\n" +
                        ");"
        );

        statements.add(
                "CREATE INDEX IF NOT EXISTS idx_users_email ON public.users (email ASC);"
        );
        statements.add(
                "CREATE INDEX IF NOT EXISTS idx_users_provider_user_id ON public.users (provider_user_id ASC);"
        );

        statements.add(
                "CREATE TABLE IF NOT EXISTS public.users_classes (\n" +
                        "  user_id integer NOT NULL,\n" +
                        "  class_id integer NOT NULL,\n" +
                        "  CONSTRAINT users_classes_pkey PRIMARY KEY (class_id, user_id)\n" +
                        ");"
        );

        // 4) Triggers (after function exists)
        statements.add(
                "DROP TRIGGER IF EXISTS trg_enforce_max_admins ON public.users;"
        );
        statements.add(
                "CREATE TRIGGER trg_enforce_max_admins\n" +
                        "  BEFORE INSERT OR UPDATE ON public.users\n" +
                        "  FOR EACH ROW EXECUTE FUNCTION public.enforce_max_admins();"
        );

        // 実行：順次
        for (String s : statements) {
            String stmt = s == null ? "" : s.trim();
            if (stmt.isEmpty()) continue;
            // 実行
            jdbc.execute(stmt);
        }
    }

    /**
     * デフォルト値を限定的に解釈して返す。
     * （この実装では使っていませんが、将来的に buildCreateTableSql と共に利用できます）
     */
    private String parseDefaultValue(String raw, String sqlType) {
        String v = raw.trim();
        if (v.equalsIgnoreCase("NOW()") || v.equalsIgnoreCase("CURRENT_TIMESTAMP")) return "CURRENT_TIMESTAMP";
        if (v.equalsIgnoreCase("TRUE") || v.equalsIgnoreCase("FALSE")) return v.toLowerCase();
        if (v.matches("^-?\\d+(\\.\\d+)?$")) return v;
        String stripped = v;
        if ((v.startsWith("'") && v.endsWith("'")) || (v.startsWith("\"") && v.endsWith("\""))) {
            stripped = v.substring(1, v.length() - 1);
        }
        String esc = stripped.replace("'", "''");
        return "'" + esc + "'";
    }
}