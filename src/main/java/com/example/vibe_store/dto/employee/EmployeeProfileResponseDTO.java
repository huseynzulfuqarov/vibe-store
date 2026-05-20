package com.example.vibe_store.dto.employee;

public record EmployeeProfileResponseDTO(
        Integer employeeId,
        String firstName,
        String lastName,
        String email,
        Byte age
) {}