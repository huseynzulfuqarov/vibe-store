package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeIdAndPayrollMonthAndStoreId(Integer employeeId, String payrollMonth, Integer storeId);

    Optional<Payroll> findByEmployeeIdAndPayrollMonth(Integer employeeId, String payrollMonth);
}