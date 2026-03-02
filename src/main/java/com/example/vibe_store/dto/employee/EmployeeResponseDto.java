package com.example.vibe_store.dto.employee;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeResponseDto {
    private String firstName;
    private String lastName;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private BigDecimal salary;
    private Byte age;
    private BigDecimal currentSalary;
    private String currentPositionName;
    private String currentStoreName;
    private String activeGradeName;
}
