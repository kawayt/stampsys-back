package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.StampManagementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StampManagementMapper {

    // 授業ごとのスタンプ一覧取得
    List<StampManagementEntity> findByClassId(@Param("classId") int classId);

    // 新しいスタンプ追加
    void insert(StampManagementEntity stamp);

    // スタンプ更新
    void update(StampManagementEntity stamp);

    // スタンプ削除
    void delete(@Param("id") int id);
}