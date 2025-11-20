package com.example.stampsysback.service;

import com.example.stampsysback.mapper.ClassMapper;
import com.example.stampsysback.mapper.UserClassMapper;
import com.example.stampsysback.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserClassServiceImpl implements UserClassService{
    private final UserClassMapper userClassMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;

    @Override
    public int addUserToClass(Integer userId, Integer classId){
        // ユーザー存在チェック
        Integer userExists = userMapper.existsById(userId);
        // いたら下へ、いなかったら（null）だったら例外
        if (userExists == null){
            throw new IllegalArgumentException("指定された userId が存在しません:" + userId);
        }

        // クラス存在チェック
        Integer classExists = classMapper.existsById(classId);
        if(classExists == null){
            throw new IllegalArgumentException("指定された classId が存在しません:" + classId);
        }

        // 指定クラスに追加ユーザーがすでに追加されているかチェック
        // 既に追加されていたら何もしない（0を返す）
        Integer rel = userClassMapper.existsRelation(userId, classId);
        // rel == 1だったら既に追加されているということ。何もしないで rel = 0 を返す
        // まだ追加されていなかったら下へ行ってinsertを実行
        if(rel != null){
            return 0;
        }

        // users_classesを追加。insertを実行して影響行数を返す（通常1）
        int rows = userClassMapper.insertUserClass(userId, classId);
        if(rows == 0){
            throw new DataAccessException("ユーザーをクラスに追加できませんでした"){};
        }
        return rows;
    }

    @Override
    public int removeUserFromClass(Integer userId, Integer classId){
        // users_classesから削除。影響行数を返す（1 = 削除した、0 = もともと存在しなかった）
        int rows = userClassMapper.deleteUserClass(userId, classId);
        return rows;
    }

}
