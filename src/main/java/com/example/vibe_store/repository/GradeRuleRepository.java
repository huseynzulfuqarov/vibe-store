package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRuleRepository extends JpaRepository<GradeRule, Integer> {

    @Query("SELECT gr FROM GradeRule gr LEFT JOIN FETCH gr.position WHERE gr.grade.id = :gradeId")
    List<GradeRule> findAllByGradeIdWithPosition(@Param("gradeId") Integer gradeId);
}