package com.example.vibe_store.dto.payroll;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PayrollResponseDTO {
    private Long payrollId;
    private Integer employeeId;
    private String employeeName;
    private String storeName;
    private BigDecimal baseSalary;
    private BigDecimal bonusAmount;
    private BigDecimal totalAmount;
    private String calculationDetails;
    private LocalDateTime createdAt;
}