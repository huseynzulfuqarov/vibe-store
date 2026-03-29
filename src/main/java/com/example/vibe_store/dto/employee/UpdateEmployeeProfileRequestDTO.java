package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeProfileRequestDTO {

    @Size(min = 4, max = 20, message = "First name length must be between 4 and 20 characters")
    private String firstName;

    @Size(min = 4, max = 20, message = "Last name length must be between 4 and 20 characters")
    private String lastName;

    @Min(value = 18, message = "Age cannot be less than 18")
    @Max(value = 65, message = "Age cannot be greater than 65")
    private Byte age;

    @Email(message = "Invalid email format")
    private String email;
}
