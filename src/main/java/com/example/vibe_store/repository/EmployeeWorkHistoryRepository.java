package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeWorkHistoryRepository extends JpaRepository<EmployeeWorkHistory,Integer> {

    Optional<EmployeeWorkHistory> findByEmployeeIdAndIsActiveTrue(Integer employeeId);
}
