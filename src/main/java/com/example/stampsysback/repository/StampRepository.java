package com.example.stampsysback.repository;

import com.example.stampsysback.entity.StampManagementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StampRepository extends JpaRepository<StampManagementEntity, Long> {

    /**
     * クラス内の各ルームごとのスタンプ（押印）総件数を一括で取得する
     * - stamp_logs テーブルを集計（押印履歴をカウント）
     * - rooms テーブルと JOIN して class_id で絞る
     *
     * ※ 実際のテーブル/カラム名が異なる場合は name を合わせてください。
     */
    @Query(value =
            "SELECT sl.room_id AS roomId, COUNT(*) AS cnt " +
                    "FROM stamp_logs sl " +
                    "JOIN rooms r ON r.room_id = sl.room_id " +
                    "WHERE r.class_id = :classId " +
                    "GROUP BY sl.room_id",
            nativeQuery = true)
    List<Object[]> findRoomStampCountsByClassId(@Param("classId") Long classId);
}