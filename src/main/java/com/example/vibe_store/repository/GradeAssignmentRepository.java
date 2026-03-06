package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeAssignmentRepository extends JpaRepository<GradeAssignment, Integer> {
    Optional<GradeAssignment> findByStoreIdAndIsActiveTrue(Integer id);
}