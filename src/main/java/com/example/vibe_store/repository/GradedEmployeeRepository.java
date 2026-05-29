package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradedEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradedEmployeeRepository extends JpaRepository<GradedEmployee, Long> {

    @Query("SELECT ge FROM GradedEmployee ge JOIN FETCH ge.employee WHERE ge.gradeAssignment.id = :assignmentId")
    List<GradedEmployee> findAllByGradeAssignmentIdWithEmployee(@Param("assignmentId") Integer assignmentId);
}