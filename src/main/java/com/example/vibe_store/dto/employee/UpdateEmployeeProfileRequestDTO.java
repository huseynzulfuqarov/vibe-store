package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeProfileRequestDTO(
        @Size(min = 4, max = 20, message = "First name length must be between 4 and 20 characters")
        String firstName,

        @Size(min = 4, max = 20, message = "Last name length must be between 4 and 20 characters")
        String lastName,

        @Min(value = 18, message = "Age cannot be less than 18")
        @Max(value = 65, message = "Age cannot be greater than 65")
        Byte age,

        @Email(message = "Invalid email format")
        String email
) {}