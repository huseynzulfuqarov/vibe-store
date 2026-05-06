package com.example.vibe_store.dto.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HireEmployeeResponseDTO {

    private AllEmployeeDetailsResponseDTO employeeDetails;

    private String generatedUsername;
    private String temporaryPassword;
}
