package com.example.vibe_store.dto.employee;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AllEmployeeDetailsResponseDTO {
    private String firstName;
    private String lastName;
    private LocalDateTime hireDate;
    private LocalDateTime terminationDate;
    private Byte age;
    private String email;
    private BigDecimal currentSalary;
    private String currentPositionName;
    private String currentStoreName;
}
