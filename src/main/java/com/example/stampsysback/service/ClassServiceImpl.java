package com.example.stampsysback.service;

import java.time.OffsetDateTime;
import java.util.List;

import com.example.stampsysback.entity.ClassEntity;
import com.example.stampsysback.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// クラスサービス実装クラス
@Service
@Transactional
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService{
    // DI
    private final ClassMapper classMapper;

    @Override
    public List<ClassEntity> selectAllClass(){
        return classMapper.selectAll();
    }
    @Override
    public void insertClass(ClassEntity classEntity){
        Integer maxId = classMapper.selectMaxId();
        classEntity.setClassId(maxId + 1); // 次のIDを設定
        classMapper.insert(classEntity);
        // return classEntity;
    }

    // classId で 1 件取得
    @Override
    public ClassEntity selectClassById(Integer classId) {
        return classMapper.selectById(classId);
    }

    // 論理削除: deleted_at に現在時刻を設定
    @Override
    public void deleteClass(Integer classId) {
        classMapper.softDelete(classId, OffsetDateTime.now());
    }

    // 復元: deleted_at を NULL に設定
    @Override
    public void restoreClass(Integer classId) {
        classMapper.restore(classId);
    }
}
