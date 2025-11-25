package com.example.stampsysback.mapper;

import com.example.stampsysback.entity.ClassStampEntity;
import com.example.stampsysback.entity.StampManagementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassStampMapper {

    // 設定画面用：全件 + 状態取得
    List<StampManagementEntity> findAllWithAssignmentStatus(@Param("classId") Integer classId);

    // クラスに紐づくスタンプを取得
    List<StampManagementEntity> findByClassId(@Param("classId") Integer classId);

    // クラスにスタンプを割り当てる
    void assignStampToClass(ClassStampEntity classStamp);

    // クラスからスタンプの割り当てを解除
    void removeStampFromClass(ClassStampEntity classStamp);
}
