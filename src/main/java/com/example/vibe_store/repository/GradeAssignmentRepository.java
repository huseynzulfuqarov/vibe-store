package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeAssignmentRepository extends JpaRepository<GradeAssignment, Integer> {
}