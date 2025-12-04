package com.example.stampsysback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * スタンプ管理エンティティ（JPA 管理対象）
 * - 必ず @Entity と @Id を付けてください
 * - テーブル名 / カラム名は実 DB スキーマに合わせて調整してください
 */
@Entity
@Table(name = "stamps") // 実テーブル名に合わせて変更（例: stamp_management 等）
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StampManagementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stamp_id") // 実 DB の PK カラム名に合わせる
    private Long stampId;

    @Column(name = "stamp_name")
    private String stampName;

    @Column(name = "stamp_icon")
    private Integer stampIcon;

    @Column(name = "stamp_color")
    private Integer stampColor;

    @Column(name = "hidden")
    private Boolean hidden; // 論理削除

    // 以下は集計や参照で便利なフィールド（DB にカラムがあればマッピングしておく）
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // assigned は DB に存在しない想定なら @Transient にする
    @Transient
    private Boolean assigned;
}