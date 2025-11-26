package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // user_idでユーザーを取得（いないuser_idを追加されないため）
    UserEntity selectById(@Param("userId") Integer userId);

    // userが存在するかだけチェックする（１を返す or null）（いないuser_idを追加されないため）
    Integer existsById(@Param("userId") Integer userId);

    // クラスに追加されているユーザー一覧を返す
    List<UserDto> selectUsersInClass(@Param("classId") Integer classId,
                                     @Param("limit") Integer limit, // 最大件数
                                     @Param("offset") Integer offset); // ページング

    // クラスに追加されてないユーザー一覧を返す
    List<UserDto> selectUsersNotInClass(@Param("classId") Integer classId,
                                        @Param("limit") Integer limit, // 最大件数
                                        @Param("offset") Integer offset); // ページング

}
