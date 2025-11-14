package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.ClassEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClassMapper {
    // クラスの情報をすべて取得、ClassEntityのリストを返す
    List<ClassEntity> selectAll();

    // クラスを作成、戻り値なし
    void insert(ClassEntity classEntity);
    Integer selectMaxId(); // 最大ID取得
}
