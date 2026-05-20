package com.example.vibe_store.dto.employee;

public record HireEmployeeResponseDTO(
        AllEmployeeDetailsResponseDTO employeeDetails,
        String generatedUsername,
        String temporaryPassword
) {}