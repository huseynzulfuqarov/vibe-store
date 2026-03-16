package com.example.vibe_store.service;

import com.example.vibe_store.dto.employee.*;

public interface EmployeeService {

    PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto);

    AllEmployeeDetailsResponseDTO hireEmployee(HireEmployeeRequestDTO hireEmployeeRequestDto);

    void changeJobDetails(ChangeJobDetailsRequestDTO changeJobDetailsRequestDto);

    EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto);

    AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId);

    PositionResponseDTO getPositionById(Integer positionId);
}
