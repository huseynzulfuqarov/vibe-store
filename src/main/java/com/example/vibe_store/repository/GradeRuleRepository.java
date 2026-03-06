package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRuleRepository extends JpaRepository<GradeRule, Integer> {
    List<GradeRule> findAllByGradeId(Integer id);
}