package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record HireEmployeeRequestDTO(
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        String lastName,

        @Min(value = 18, message = "Age cannot be less than 18")
        @Max(value = 65, message = "Age cannot be greater than 65")
        @NotNull
        Byte age,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Please enter a valid email format")
        String email,

        @NotNull(message = "Store cannot be null")
        Integer storeId,

        @NotNull(message = "Position cannot be null")
        Integer positionId,

        @NotNull(message = "Salary cannot be null")
        @DecimalMin(value = "370", message = "Minimum salary is 370 AZN")
        BigDecimal salary
) {}