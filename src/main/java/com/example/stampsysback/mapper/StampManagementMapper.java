package com.example.stampsysback.mapper;

import com.example.stampsysback.dto.StampManagementRequest;
import com.example.stampsysback.dto.StampManagementResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StampManagementMapper {

    // スタンプ一覧取得
    List<StampManagementResponse> findAll();

    // 指定クラスに紐づくスタンプ
    List<StampManagementResponse> selectStampsByClassId(@Param("classId") Integer classId);

    // 指定クラスに紐づいていないスタンプ
    List<StampManagementResponse> selectStampsNotInClassId(@Param("classId") Integer classId);

    // 新しいスタンプ追加
    void insert(StampManagementRequest dto);

    // スタンプ論理削除
    void delete(@Param("stampId") int stampId);


}
