package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradedEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradedEmployeeRepository extends JpaRepository<GradedEmployee, Long> {

    List<GradedEmployee> findAllByGradeAssignmentId(Integer assignmentId);
}