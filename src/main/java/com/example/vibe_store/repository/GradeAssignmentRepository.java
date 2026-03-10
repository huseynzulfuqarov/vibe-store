package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GradeAssignmentRepository extends JpaRepository<GradeAssignment, Integer> {
    Optional<GradeAssignment> findByStoreIdAndIsActiveTrue(Integer id);

    @Query("SELECT ga FROM GradeAssignment ga " +
            "WHERE ga.store.id = :storeId " +
            "AND ga.isActive = true " +
            "AND ga.endDate >= :startOfMonth AND ga.endDate <= :endOfMonth")
    List<GradeAssignment> findByStoreIdAndEndDateInTargetMonth(
            @Param("storeId") Integer storeId,
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("endOfMonth") LocalDateTime endOfMonth
    );
}