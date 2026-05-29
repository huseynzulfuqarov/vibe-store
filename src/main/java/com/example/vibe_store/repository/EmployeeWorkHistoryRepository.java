package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeWorkHistoryRepository extends JpaRepository<EmployeeWorkHistory, Integer> {

    Optional<EmployeeWorkHistory> findByEmployeeIdAndIsActiveTrue(Integer employeeId);

    @Query(value = "SELECT ewh FROM EmployeeWorkHistory ewh " +
                   "JOIN FETCH ewh.employee " +
                   "JOIN FETCH ewh.store " +
                   "JOIN FETCH ewh.position " +
                   "WHERE ewh.isActive = true",
           countQuery = "SELECT count(ewh) FROM EmployeeWorkHistory ewh WHERE ewh.isActive = true")
    Page<EmployeeWorkHistory> findAllActiveWithDetails(Pageable pageable);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
           "JOIN FETCH ewh.employee " +
           "JOIN FETCH ewh.store " +
           "JOIN FETCH ewh.position " +
           "WHERE ewh.employee.id = :employeeId AND ewh.isActive = true")
    Optional<EmployeeWorkHistory> findByEmployeeIdAndIsActiveTrueWithDetails(@Param("employeeId") Integer employeeId);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
            "JOIN FETCH ewh.employee " +
            "JOIN FETCH ewh.store " +
            "JOIN FETCH ewh.position " +
            "WHERE ewh.store.id = :storeId " +
            "AND ewh.startDate <= :monthEnd " +
            "AND (ewh.endDate IS NULL OR ewh.endDate >= :monthStart)")
    List<EmployeeWorkHistory> findAllWorkedInStoreAndMonthWithDetails(
            @Param("storeId") Integer storeId,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEnd") LocalDateTime monthEnd);

    Optional<EmployeeWorkHistory> findByEmployeeIdAndStoreIdAndIsActiveTrue(Integer employeeId, Integer storeId);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
            "JOIN FETCH ewh.employee " +
            "JOIN FETCH ewh.store " +
            "JOIN FETCH ewh.position " +
            "WHERE ewh.employee.id = :employeeId " +
            "AND ewh.startDate <= :monthEnd " +
            "AND (ewh.endDate IS NULL OR ewh.endDate >= :monthStart)")
    List<EmployeeWorkHistory> findAllByEmployeeIdAndMonthWithDetails(
            @Param("employeeId") Integer employeeId,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEnd") LocalDateTime monthEnd);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
           "JOIN FETCH ewh.employee " +
           "JOIN FETCH ewh.store " +
           "JOIN FETCH ewh.position " +
           "WHERE ewh.store.id = :storeId AND ewh.isActive = true")
    List<EmployeeWorkHistory> findAllActiveByStoreIdWithDetails(@Param("storeId") Integer storeId);
}