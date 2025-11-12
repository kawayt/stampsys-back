package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.StampManagementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StampManagementMapper {

    // スタンプ一覧取得
    List<StampManagementEntity> findAll();

    // 新しいスタンプ追加
    void insert(StampManagementEntity stamp);

    // スタンプ削除
    void delete(@Param("stampId") int stampId);
}
