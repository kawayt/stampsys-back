package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.repository.UserRepository;
import com.example.stampsysback.service.AdminAuditLogService;
import com.example.stampsysback.service.DatabaseSetupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * AdminDbController（改良版）
 *
 * - テーブル名/カラム名のバリデーション
 * - PK列のデータ型を調べて、id を適切な Java 型にパースしてから JDBC に渡す
 * - 主要な例外ハンドリングと依存チェック（users 等）を導入
 */
@RestController
@RequestMapping("/api/admin/db")
public class AdminDbController {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSetupService databaseSetupService;
    private final AdminAuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    public AdminDbController(JdbcTemplate jdbcTemplate,
                             DatabaseSetupService databaseSetupService,
                             AdminAuditLogService auditLogService,
                             UserRepository userRepository,
                             ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseSetupService = databaseSetupService;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private void validateIdentifier(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("不正な識別子です: " + name);
        }
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("認証情報からユーザーを特定できません");
        }
        String providerUserId = oidcUser.getSubject();
        return userRepository.findByProviderUserId(providerUserId)
                .orElseThrow(() -> new IllegalStateException("DB上のユーザー情報が見つかりません"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        ex.printStackTrace();
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(409).body(Map.of("message", msg == null ? "データベースエラー" : msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace();
        String msg = ex.getMessage();
        return ResponseEntity.status(500).body(Map.of("message", msg == null ? "サーバー側で予期しないエラーが発生しました" : msg));
    }

    @GetMapping("/tables")
    public List<Map<String, Object>> listTables() {
        List<String> names = databaseSetupService.listPublicTables();
        List<Map<String, Object>> result = new ArrayList<>();
        for (String n : names) {
            Map<String, Object> row = new HashMap<>();
            row.put("tableName", n);
            result.add(row);
        }
        return result;
    }

    @GetMapping("/tables/{tableName}/columns")
    public ResponseEntity<?> listColumns(@PathVariable("tableName") String tableName) {
        try {
            validateIdentifier(tableName);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

        String pkSql =
                "SELECT a.attname AS column_name " +
                        "FROM pg_index i " +
                        "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                        "JOIN pg_class c ON c.oid = i.indrelid " +
                        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                        "WHERE n.nspname = 'public' " +
                        "  AND c.relname = ? " +
                        "  AND i.indisprimary";

        Set<String> pkColumns = new HashSet<>(jdbcTemplate.queryForList(pkSql, String.class, tableName));

        String colSql =
                "SELECT column_name, data_type, is_nullable, column_default " +
                        "FROM information_schema.columns " +
                        "WHERE table_schema = 'public' AND table_name = ? " +
                        "ORDER BY ordinal_position";

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(colSql, tableName);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> col : columns) {
            String colName = (String) col.get("column_name");
            String dataType = (String) col.get("data_type");
            String isNullable = (String) col.get("is_nullable");
            Object columnDefault = col.get("column_default");

            Map<String, Object> row = new HashMap<>();
            row.put("name", colName);
            row.put("dataType", dataType);
            row.put("isNullable", "YES".equalsIgnoreCase(isNullable));
            row.put("isPrimaryKey", pkColumns.contains(colName));
            row.put("isUnique", false);
            row.put("columnDefault", columnDefault);
            result.add(row);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tables/{tableName}/rows")
    public ResponseEntity<?> listRows(@PathVariable("tableName") String tableName,
                                      @RequestParam(name = "limit", defaultValue = "50") int limit,
                                      @RequestParam(name = "offset", defaultValue = "0") int offset,
                                      Authentication authentication) {
        try {
            validateIdentifier(tableName);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

        if (limit < 1 || limit > 500) {
            return ResponseEntity.badRequest().body(Map.of("message", "limit は 1〜500 の範囲で指定してください"));
        }
        if (offset < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "offset は 0 以上で指定してください"));
        }

        String countSql = "SELECT COUNT(*) FROM public." + tableName;
        long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);

        String dataSql = "SELECT * FROM public." + tableName + " ORDER BY 1 LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, limit, offset);

        try {
            User admin = resolveCurrentUser(authentication);
            auditLogService.log(
                    admin.getUserId(),
                    admin.getEmail(),
                    tableName,
                    "SELECT",
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);
        result.put("totalCount", totalCount);
        result.put("limit", limit);
        result.put("offset", offset);

        return ResponseEntity.ok(result);
    }

    private List<String> getPrimaryKeyColumns(String tableName) {
        String pkSql =
                "SELECT a.attname AS column_name " +
                        "FROM pg_index i " +
                        "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) " +
                        "JOIN pg_class c ON c.oid = i.indrelid " +
                        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                        "WHERE n.nspname = 'public' " +
                        "  AND c.relname = ? " +
                        "  AND i.indisprimary " +
                        "ORDER BY a.attnum";

        return jdbcTemplate.queryForList(pkSql, String.class, tableName);
    }

    /**
     * 指定テーブルの指定カラムの data_type を返す（information_schema.columns.data_type）
     */
    private String getColumnDataType(String tableName, String columnName) {
        String sql = "SELECT data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?";
        List<String> types = jdbcTemplate.queryForList(sql, String.class, tableName, columnName);
        return types.isEmpty() ? null : types.get(0);
    }

    /**
     * 主キー文字列 (path variable) を適切な Java オブジェクトに変換して返す。
     * - integer -> Integer
     * - bigint -> Long
     * - character varying / text -> String
     * - その他: String を返す
     *
     * 変換できない場合は IllegalArgumentException を投げる。
     */
    private Object parseIdValue(String tableName, String pkColumn, String idStr) {
        String dataType = getColumnDataType(tableName, pkColumn);
        if (dataType == null) {
            // 型が取れない場合は文字列のまま渡す（フォールバック）
            return idStr;
        }
        dataType = dataType.toLowerCase(Locale.ROOT);
        try {
            if (dataType.contains("integer") && !dataType.contains("big")) {
                return Integer.valueOf(idStr);
            } else if (dataType.contains("bigint") || dataType.contains("big serial")) {
                return Long.valueOf(idStr);
            } else if (dataType.contains("numeric") || dataType.contains("decimal")) {
                // 必要なら BigDecimal に変更可能
                return new java.math.BigDecimal(idStr);
            } else {
                // text, character varying, uuid, etc.
                return idStr;
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("主キーの値の形式が正しくありません（期待される型: " + dataType + "）: " + idStr);
        }
    }

    @PostMapping("/tables/{tableName}/rows")
    public ResponseEntity<?> insertRow(@PathVariable("tableName") String tableName,
                                       @RequestBody Map<String, Object> body,
                                       Authentication authentication) {
        try {
            validateIdentifier(tableName);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "挿入するデータが空です"));
        }

        String colSql =
                "SELECT column_name " +
                        "FROM information_schema.columns " +
                        "WHERE table_schema = 'public' AND table_name = ?";

        Set<String> validColumns = new HashSet<>(jdbcTemplate.queryForList(colSql, String.class, tableName));

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String colName = entry.getKey();
            if (!IDENTIFIER.matcher(colName).matches()) {
                continue;
            }
            if (!validColumns.contains(colName)) {
                continue;
            }
            columns.add(colName);
            values.add(entry.getValue());
        }

        if (columns.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "有効なカラムが指定されていません"));
        }

        StringJoiner colJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        for (String c : columns) {
            colJoiner.add(c);
            placeholderJoiner.add("?");
        }

        String sql = "INSERT INTO public." + tableName +
                " (" + colJoiner + ") VALUES (" + placeholderJoiner + ")";

        jdbcTemplate.update(sql, values.toArray());

        try {
            User admin = resolveCurrentUser(authentication);
            String afterJson = objectMapper.writeValueAsString(body);
            auditLogService.log(
                    admin.getUserId(),
                    admin.getEmail(),
                    tableName,
                    "INSERT",
                    null,
                    null,
                    afterJson
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "1件挿入しました"));
    }
// （ファイル全体は既にある前提。ここでは updateRow メソッド周辺の変更を示します）

    @PutMapping("/tables/{tableName}/rows/{id}")
    public ResponseEntity<?> updateRow(@PathVariable("tableName") String tableName,
                                       @PathVariable("id") String id,
                                       @RequestBody Map<String, Object> body,
                                       Authentication authentication) {
        try {
            validateIdentifier(tableName);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "更新するデータが空です"));
        }

        List<String> pkColumns = getPrimaryKeyColumns(tableName);
        if (pkColumns.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "主キーが定義されていないテーブルは更新できません"));
        }
        if (pkColumns.size() > 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "複合主キーのテーブルはこのエンドポイントから更新できません"));
        }
        String pkColumn = pkColumns.get(0);

        // 主キーの値を適切にパース
        Object idParam = parseIdValue(tableName, pkColumn, id);

        String selectSql = "SELECT * FROM public." + tableName + " WHERE " + pkColumn + " = ?";
        List<Map<String, Object>> beforeList = jdbcTemplate.queryForList(selectSql, idParam);
        Map<String, Object> beforeRow = beforeList.isEmpty() ? null : beforeList.get(0);

        // カラム一覧と型情報を取得する
        String colInfoSql = "SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ?";
        List<Map<String, Object>> colInfoList = jdbcTemplate.queryForList(colInfoSql, tableName);
        Set<String> validColumns = new HashSet<>();
        Map<String, String> colTypeMap = new HashMap<>();
        for (Map<String, Object> r : colInfoList) {
            String colName = (String) r.get("column_name");
            String dataType = (String) r.get("data_type");
            validColumns.add(colName);
            colTypeMap.put(colName, dataType == null ? "" : dataType.toLowerCase(Locale.ROOT));
        }

        // 更新不許可の型一覧（timestamp / timestamptz / date など）
        Set<String> forbiddenTypeSubstrings = Set.of("timestamp", "time", "date");

        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String colName = entry.getKey();
            // 名前チェック、存在チェック、PK除外
            if (!IDENTIFIER.matcher(colName).matches()) continue;
            if (!validColumns.contains(colName)) continue;
            if (colName.equals(pkColumn)) continue;

            // 型を見て更新不可ならスキップ
            String dtype = colTypeMap.getOrDefault(colName, "");
            boolean isForbidden = false;
            for (String sub : forbiddenTypeSubstrings) {
                if (dtype.contains(sub)) {
                    isForbidden = true;
                    break;
                }
            }
            if (isForbidden) {
                // 念のためログ出力してスキップ
                System.out.println("Skipping update of column (type restricted): " + colName + " (" + dtype + ")");
                continue;
            }

            setClauses.add(colName + " = ?");
            values.add(entry.getValue());
        }

        if (setClauses.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "有効な更新対象カラムが指定されていません（更新可能なカラムがありません）"));
        }

        StringJoiner setJoiner = new StringJoiner(", ");
        for (String clause : setClauses) setJoiner.add(clause);

        String updateSql = "UPDATE public." + tableName + " SET " + setJoiner + " WHERE " + pkColumn + " = ?";
        values.add(idParam);

        int updated = jdbcTemplate.update(updateSql, values.toArray());
        if (updated == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "対象行が見つからないか、更新されませんでした"));
        }

        List<Map<String, Object>> afterList = jdbcTemplate.queryForList(selectSql, idParam);
        Map<String, Object> afterRow = afterList.isEmpty() ? null : afterList.get(0);

        try {
            User admin = resolveCurrentUser(authentication);
            String beforeJson = beforeRow == null ? null : objectMapper.writeValueAsString(beforeRow);
            String afterJson = afterRow == null ? null : objectMapper.writeValueAsString(afterRow);
            String pkInfo = pkColumn + "=" + id;
            auditLogService.log(admin.getUserId(), admin.getEmail(), tableName, "UPDATE", pkInfo, beforeJson, afterJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "1件更新しました"));
    }


    @DeleteMapping("/tables/{tableName}/rows/{id}")
    public ResponseEntity<?> deleteRow(@PathVariable("tableName") String tableName,
                                       @PathVariable("id") String id,
                                       Authentication authentication) {
        try {
            validateIdentifier(tableName);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

        List<String> pkColumns = getPrimaryKeyColumns(tableName);
        if (pkColumns.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "主キーが定義されていないテーブルは削除できません"));
        }
        if (pkColumns.size() > 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "複合主キーのテーブルはこのエンドポイントから削除できません"));
        }
        String pkColumn = pkColumns.get(0);

        Object idParam = parseIdValue(tableName, pkColumn, id);

        // users テーブル向けの参照チェック（例）
        if ("users".equals(tableName)) {
            int stampLogs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.stamp_logs WHERE user_id = ?", Integer.class, idParam);
            int usersClasses = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public.users_classes WHERE user_id = ?", Integer.class, idParam);
            if (stampLogs > 0 || usersClasses > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("このユーザーは他のデータに参照されています。");
                if (stampLogs > 0) sb.append(" stamp_logs:").append(stampLogs).append("件");
                if (usersClasses > 0) sb.append(" users_classes:").append(usersClasses).append("件");
                sb.append("。先に関連データを削除または参照を解除してください。");
                return ResponseEntity.status(409).body(Map.of("message", sb.toString()));
            }
        }

        String selectSql = "SELECT * FROM public." + tableName + " WHERE " + pkColumn + " = ?";
        List<Map<String, Object>> beforeList = jdbcTemplate.queryForList(selectSql, idParam);
        Map<String, Object> beforeRow = beforeList.isEmpty() ? null : beforeList.get(0);

        String deleteSql = "DELETE FROM public." + tableName + " WHERE " + pkColumn + " = ?";
        int deleted = jdbcTemplate.update(deleteSql, idParam);
        if (deleted == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "対象行が見つからないか、削除されませんでした"));
        }

        try {
            User admin = resolveCurrentUser(authentication);
            String beforeJson = beforeRow == null ? null : objectMapper.writeValueAsString(beforeRow);
            String pkInfo = pkColumn + "=" + id;

            auditLogService.log(
                    admin.getUserId(),
                    admin.getEmail(),
                    tableName,
                    "DELETE",
                    pkInfo,
                    beforeJson,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "1件削除しました"));
    }
}