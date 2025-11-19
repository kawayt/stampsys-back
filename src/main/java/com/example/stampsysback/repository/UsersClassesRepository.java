package com.example.stampsysback.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * users_classes テーブルに対する最小限のリポジトリ。
 * JPA エンティティを作らずに存在チェックのみ行うため JdbcTemplate を使用しています。
 * テーブル／カラム名が実際と違う場合は SQL を修正してください。
 */
@Repository
public class UsersClassesRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsersClassesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 指定 userId, classId の組み合わせが存在するかを返す。
     */
    public boolean existsByUserIdAndClassId(Integer userId, Integer classId) {
        if (userId == null || classId == null) return false;
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM users_classes uc WHERE uc.user_id = ? AND uc.class_id = ?",
                    Integer.class, userId, classId);
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }
}