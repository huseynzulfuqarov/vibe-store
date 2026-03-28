package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ChangeJobDetailsRequestDTO {

    @NotNull(message = "Employee ID cannot be empty")
    private Integer employeeId;

    private Integer targetStoreId;
    private Integer targetPositionId;

    @Min(value = 370, message = "Salary cannot be less than 370 azn")
    private BigDecimal newSalary;
}
