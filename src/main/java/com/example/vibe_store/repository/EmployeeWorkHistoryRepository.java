package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeWorkHistoryRepository extends JpaRepository<EmployeeWorkHistory, Integer> {

    Optional<EmployeeWorkHistory> findByEmployeeIdAndIsActiveTrue(Integer employeeId);

    List<EmployeeWorkHistory> findAllByIsActiveTrue();

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
            "WHERE ewh.store.id = :storeId " +
            "AND ewh.isActive = true")
    List<EmployeeWorkHistory> findAllActiveByStoreId(@Param("storeId") Integer storeId);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
            "WHERE ewh.store.id = :storeId " +
            "AND ewh.startDate <= :monthEnd " +
            "AND (ewh.endDate IS NULL OR ewh.endDate >= :monthStart)")
    List<EmployeeWorkHistory> findAllWorkedInStoreAndMonth(
            @Param("storeId") Integer storeId,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEnd") LocalDateTime monthEnd);

    Optional<EmployeeWorkHistory> findByEmployeeIdAndStoreIdAndIsActiveTrue(Integer employeeId, Integer storeId);

    @Query("SELECT ewh FROM EmployeeWorkHistory ewh " +
            "WHERE ewh.employee.id = :employeeId " +
            "AND ewh.startDate <= :monthEnd " +
            "AND (ewh.endDate IS NULL OR ewh.endDate >= :monthStart)")
    List<EmployeeWorkHistory> findAllByEmployeeIdAndMonth(
            @Param("employeeId") Integer employeeId,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("monthEnd") LocalDateTime monthEnd);

}