package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // user_idでユーザーを取得（いないuser_idを追加されないため）
    UserEntity selectById(@Param("userId") Integer userId);

    // userが存在するかだけチェックする（１を返す or null）（いないuser_idを追加されないため）
    Integer existsById(@Param("userId") Integer userId);
}
