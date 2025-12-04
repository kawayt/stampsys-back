package com.example.stampsysback.repository;

import com.example.stampsysback.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {
    // findAll() を使うだけの単純リポジトリ
}