package com.example.stampsysback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsersClassesMapper {
    /**
     * 指定 userId, classId の組み合わせが存在するかを boolean で返す。
     * MyBatis XML 側で SELECT EXISTS(...) を実行し、Boolean を返す想定。
     */
    Boolean existsByUserIdAndClassId(@Param("userId") Integer userId, @Param("classId") Integer classId);
}