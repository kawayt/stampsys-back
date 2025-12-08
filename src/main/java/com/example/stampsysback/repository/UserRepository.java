package com.example.stampsysback.repository;

import com.example.stampsysback.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findByHiddenFalse(Pageable pageable);
    List<User> findByHiddenFalse();

    Page<User> findByRoleAndHiddenFalse(String role, Pageable pageable);
    List<User> findByRoleAndHiddenFalse(String role);

    List<User> findByHiddenTrue();
    Page<User> findByRoleAndHiddenTrue(String role, Pageable pageable);

    long countByHiddenFalse();
    long countByRoleAndHiddenFalse(String role);

    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByEmail(String email);

    Long countByRole(String admin);

    @Modifying
    @Query("UPDATE User u SET u.groupId = NULL WHERE u.groupId = :groupId")
    void setGroupIdToNull(@Param("groupId") Integer groupId);
    @Query("SELECT u.groupId, COUNT(u) FROM User u WHERE u.hidden = false GROUP BY u.groupId")
    List<Object[]> countUsersByGroup();

    /**
     * hidden=false 固定。role/groupId/keyword を任意で指定可能な検索。
     * - role が null/空/"ALL" のとき無視
     * - groupId が null のとき無視、-1 のとき「未所属(null)」、それ以外は一致
     * - keyword が null/空のとき無視。userName/email を部分一致（大小無視）で検索
     */
    @Query("""
    SELECT u FROM User u
    WHERE u.hidden = false
      AND (:role IS NULL OR :role = '' OR UPPER(:role) = 'ALL' OR UPPER(u.role) = UPPER(:role))
      AND (
            :groupId IS NULL
            OR (:groupId = -1 AND u.groupId IS NULL)
            OR (:groupId <> -1 AND u.groupId = :groupId)
      )
      AND (
            :keyword IS NULL OR :keyword = ''
            OR LOWER(u.userName) LIKE CONCAT('%', LOWER(:keyword), '%')
            OR LOWER(u.email)   LIKE CONCAT('%', LOWER(:keyword), '%')
      )
""")
    Page<User> searchVisible(@Param("keyword") String keyword,
                             @Param("role") String role,
                             @Param("groupId") Integer groupId,
                             Pageable pageable);
}