package com.example.vibe_store.dto.payroll;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayrollResponseDTO(
        Long payrollId,
        Integer employeeId,
        String employeeName,
        String storeName,
        BigDecimal baseSalary,
        BigDecimal bonusAmount,
        BigDecimal totalAmount,
        String calculationDetails,
        LocalDateTime createdAt
) {}