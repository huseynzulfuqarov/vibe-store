package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Integer> {
}