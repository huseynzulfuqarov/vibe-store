package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HireEmployeeRequestDTO {

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Min(value = 18, message = "Age cannot be less than 18")
    @Max(value = 65, message = "Age cannot be greater than 65")
    private Byte age;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please enter a valid email format")
    private String email;

    @NotNull(message = "Store cannot be null")
    private Integer storeId;

    @NotNull(message = "Position cannot be null")
    private Integer positionId;

    @NotNull(message = "Salary cannot be null")
    @Min(value = 370, message = "Minimum salary is 370 AZN")
    private BigDecimal salary;
}