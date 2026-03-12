package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeIdAndPayrollMonth(Integer employeeId, String payrollMonth);

    boolean existsByEmployeeIdAndPayrollMonth(Integer employeeId, String payrollMonth);
}