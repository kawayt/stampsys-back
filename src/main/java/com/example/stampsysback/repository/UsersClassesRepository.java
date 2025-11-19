package com.example.stampsysback.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class UsersClassesRepository {

    private static final Logger log = LoggerFactory.getLogger(UsersClassesRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UsersClassesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 指定 userId, classId の組み合わせが存在するかを返す。
     * PostgreSQL の場合は SELECT EXISTS(...) で boolean を直接取得します。
     */
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndClassId(Integer userId, Integer classId) {
        if (userId == null || classId == null) return false;
        try {
            Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM users_classes uc WHERE uc.user_id = ? AND uc.class_id = ?)",
                    Boolean.class, userId, classId);
            return Boolean.TRUE.equals(exists);
        } catch (Exception ex) {
            log.error("Failed to check users_classes existence for userId={}, classId={}", userId, classId, ex);
            // DB障害時は上位で扱いたいならここで例外を投げる。現状の設計に合わせるなら false を返す。
            return false;
        }
    }
}