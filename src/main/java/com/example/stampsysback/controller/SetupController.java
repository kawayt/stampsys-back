package com.example.stampsysback.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

    private static final Logger logger = LoggerFactory.getLogger(SetupController.class);

    @Value("${app.setup.secret:}")
    private String setupSecret;

    @Value("${app.setup.default-jdbc-url:}")
    private String defaultJdbcUrl;
    @Value("${app.setup.default-username:}")
    private String defaultUsername;
    @Value("${app.setup.default-password:}")
    private String defaultPassword;

    /**
     * 安全なステータスチェック。例外はキャッチして HTTP 200 + 詳細 JSON を返す（フロントが期待する形式）
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> res = new HashMap<>();
        res.put("configuredJdbcUrl", defaultJdbcUrl == null || defaultJdbcUrl.isBlank() ? "" : defaultJdbcUrl);
        // 解析情報をつける（ホスト/ポート/DB名）
        res.put("parsed", parseJdbcUrl(defaultJdbcUrl));

        if (defaultJdbcUrl == null || defaultJdbcUrl.isBlank()) {
            res.put("ok", false);
            res.put("status", "no-config");
            res.put("message", "app.setup.default-jdbc-url が設定されていません");
            return ResponseEntity.ok(res);
        }

        try (Connection conn = DriverManager.getConnection(defaultJdbcUrl, defaultUsername, defaultPassword)) {
            DatabaseMetaData md = conn.getMetaData();
            res.put("ok", true);
            res.put("connectionOk", true);
            res.put("databaseProductName", md.getDatabaseProductName());
            res.put("databaseProductVersion", md.getDatabaseProductVersion());
            res.put("message", "DB に接続できました");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            // ここで例外を握りつぶして詳細を JSON で返す（HTTP 500 を直接返さない）
            logger.warn("status check failed: {}", e.getMessage());
            res.put("ok", false);
            res.put("connectionOk", false);
            res.put("message", e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @PostMapping("/init")
    public ResponseEntity<?> initialize(
            @RequestHeader(value = "X-Setup-Token", required = false) String token,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        logger.debug("Setup init requested (token provided? {})", token != null);

        if (setupSecret != null && !setupSecret.isBlank()) {
            if (token == null || !token.trim().equals(setupSecret.trim())) {
                logger.warn("Invalid setup token (provided? {})", token != null);
                return ResponseEntity.status(403).body(Map.of("ok", false, "error", "invalid setup token"));
            }
        }

        String jdbcUrl = null;
        String username = null;
        String password = null;
        if (body != null) {
            jdbcUrl = (String) body.getOrDefault("jdbcUrl", "");
            username = (String) body.getOrDefault("username", "");
            password = (String) body.getOrDefault("password", "");
        }
        if (jdbcUrl == null || jdbcUrl.isBlank()) jdbcUrl = defaultJdbcUrl;
        if (username == null || username.isBlank()) username = defaultUsername;
        if (password == null || password.isBlank()) password = defaultPassword;

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "jdbcUrl is required (either in request body or app.setup.default-jdbc-url)"));
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            boolean prevAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {

                // Ensure sequences exist
                stmt.execute("CREATE SEQUENCE IF NOT EXISTS users_user_id_seq");
                stmt.execute("CREATE SEQUENCE IF NOT EXISTS stamp_logs_stamp_log_id_seq");

                // Ensure critical columns exist (ALTER TABLE ADD COLUMN IF NOT EXISTS)
                stmt.execute("ALTER TABLE public.users ADD COLUMN IF NOT EXISTS hidden boolean NOT NULL DEFAULT false");

                // Create function only if not exists (do not overwrite)
                boolean funcExists = false;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM pg_proc WHERE proname = ?")) {
                    ps.setString(1, "enforce_max_admins");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) funcExists = true;
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to check enforce_max_admins existence: {}", e.getMessage());
                }

                if (!funcExists) {
                    String createFunc =
                            "CREATE FUNCTION public.enforce_max_admins() RETURNS trigger LANGUAGE plpgsql AS $$\n" +
                                    "BEGIN\n" +
                                    "  RETURN NEW;\n" +
                                    "END;\n" +
                                    "$$;";
                    stmt.execute(createFunc);
                    logger.info("Created placeholder enforce_max_admins()");
                } else {
                    logger.info("enforce_max_admins() exists; not replacing.");
                }

                // Create tables if not exists
                stmt.execute("CREATE TABLE IF NOT EXISTS public.classes ( class_id integer NOT NULL, class_name varchar(255) NOT NULL, created_at timestamptz NOT NULL, CONSTRAINT classes_pkey PRIMARY KEY (class_id) )");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.rooms ( room_id integer NOT NULL, room_name varchar(255) NOT NULL, class_id integer NOT NULL, active boolean NOT NULL, created_at timestamptz NOT NULL, hidden boolean NOT NULL DEFAULT false, CONSTRAINT rooms_pkey PRIMARY KEY (room_id) )");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.stamp_logs ( user_id integer NOT NULL, room_id integer NOT NULL, stamp_id integer NOT NULL, sent_at timestamptz NOT NULL DEFAULT now(), stamp_log_id integer NOT NULL DEFAULT nextval('stamp_logs_stamp_log_id_seq'::regclass), CONSTRAINT stamp_logs_pkey PRIMARY KEY (stamp_log_id) )");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.stamps ( stamp_id integer NOT NULL DEFAULT nextval('users_user_id_seq'::regclass), stamp_name varchar(255) NOT NULL, stamp_color integer NOT NULL, stamp_icon integer NOT NULL, stamp_deleted boolean NOT NULL DEFAULT false, CONSTRAINT stamps_pkey PRIMARY KEY (stamp_id) )");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.stamps_classes ( stamp_id integer NOT NULL, class_id integer NOT NULL, CONSTRAINT stamps_classes_pkey PRIMARY KEY (stamp_id, class_id) )");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.users ( user_name varchar(255) NOT NULL, provider_user_id varchar(255) NOT NULL, email varchar(255) NOT NULL, role varchar(255) NOT NULL, created_at timestamptz NOT NULL, user_id integer NOT NULL DEFAULT nextval('users_user_id_seq'::regclass), hidden boolean NOT NULL DEFAULT false, CONSTRAINT users_pkey PRIMARY KEY (user_id), CONSTRAINT uk_users_email UNIQUE (email), CONSTRAINT uk_users_provider_user_id UNIQUE (provider_user_id), CONSTRAINT users_role_check CHECK (role = ANY (ARRAY['ADMIN','STUDENT','TEACHER'])) )");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON public.users (email ASC)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_provider_user_id ON public.users (provider_user_id ASC)");
                stmt.execute("CREATE TABLE IF NOT EXISTS public.users_classes ( user_id integer NOT NULL, class_id integer NOT NULL, CONSTRAINT users_classes_pkey PRIMARY KEY (class_id, user_id) )");

                // Triggers
                stmt.execute("DROP TRIGGER IF EXISTS trg_enforce_max_admins ON public.users");
                stmt.execute("CREATE TRIGGER trg_enforce_max_admins BEFORE INSERT OR UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.enforce_max_admins()");

                conn.commit();
                conn.setAutoCommit(prevAuto);

                return ResponseEntity.ok(Map.of("ok", true, "message", "初期テーブル群を作成しました", "timestamp", OffsetDateTime.now().toString()));
            } catch (Exception ex) {
                try { conn.rollback(); } catch (SQLException se) { logger.error("Rollback failed", se); }
                logger.error("Initialization failed", ex);
                return ResponseEntity.status(500).body(Map.of("ok", false, "error", ex.getMessage()));
            } finally {
                try { conn.setAutoCommit(prevAuto); } catch (SQLException ignored) {}
            }
        } catch (Exception e) {
            logger.error("Connection/Init failed", e);
            return ResponseEntity.status(500).body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // 簡易 JDBC URL パーサ（jdbc:postgresql://host:port/dbname 形式を想定）
    private Map<String, Object> parseJdbcUrl(String jdbcUrl) {
        Map<String, Object> out = new HashMap<>();
        out.put("host", null);
        out.put("port", null);
        out.put("database", null);
        if (jdbcUrl == null) return out;
        Pattern p = Pattern.compile("^jdbc:postgresql:\\/\\/([^:\\/]+)(?::(\\d+))?\\/(?:(\\w+).*)?$");
        Matcher m = p.matcher(jdbcUrl);
        if (m.find()) {
            out.put("host", m.group(1));
            out.put("port", m.group(2));
            out.put("database", m.group(3));
        }
        return out;
    }
}