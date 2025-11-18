package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.ClassEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassMapper {
    // クラスの情報をすべて取得、ClassEntityのリストを返す
    List<ClassEntity> selectAll();

    // クラスを作成、戻り値なし
    void insert(ClassEntity classEntity);

    // 現在の最大class_idを返す（存在しなければ０）
    Integer selectMaxId(); // 最大ID取得

    // classIdの存在確認、存在すれば1、存在しなければ0を返す
    Integer existsById(@Param("classId") Integer classId);
}
