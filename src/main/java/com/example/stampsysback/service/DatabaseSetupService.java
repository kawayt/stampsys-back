package com.example.stampsysback.service;

import java.util.List;

/**
 * Database setup helper - interface と DTO をここにまとめる
 */
public interface DatabaseSetupService {

    class ColumnDef {
        private String name;
        private String type; // e.g. VARCHAR, INTEGER, SERIAL, TIMESTAMP, TEXT, BOOLEAN, BIGINT
        private Integer length; // varchar length
        private boolean notNull;
        private boolean primaryKey;
        private boolean unique;
        private boolean autoIncrement; // prefers SERIAL / BIGSERIAL when true
        private String defaultValue; // literal (limited support)

        public ColumnDef() {}

        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getLength() { return length; }
        public void setLength(Integer length) { this.length = length; }
        public boolean isNotNull() { return notNull; }
        public void setNotNull(boolean notNull) { this.notNull = notNull; }
        public boolean isPrimaryKey() { return primaryKey; }
        public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
        public boolean isUnique() { return unique; }
        public void setUnique(boolean unique) { this.unique = unique; }
        public boolean isAutoIncrement() { return autoIncrement; }
        public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    }

    class CreateTableRequest {
        private String tableName;
        private List<ColumnDef> columns;
        private boolean ifNotExists = true;

        public CreateTableRequest() {}

        // getters / setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public List<ColumnDef> getColumns() { return columns; }
        public void setColumns(List<ColumnDef> columns) { this.columns = columns; }
        public boolean isIfNotExists() { return ifNotExists; }
        public void setIfNotExists(boolean ifNotExists) { this.ifNotExists = ifNotExists; }
    }

    /**
     * SQL を組み立てて返す（バリデーションはここで行う）
     */
    String buildCreateTableSql(CreateTableRequest req);

    /**
     * 実際に SQL を実行する
     */
    void executeCreateTable(CreateTableRequest req, String sql);

    /**
     * public スキーマ内にあるテーブル一覧を返す
     */
    List<String> listPublicTables();


    void initializeSchema();
}