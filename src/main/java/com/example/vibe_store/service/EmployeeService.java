package com.example.vibe_store.service;

import com.example.vibe_store.dto.employee.*;

public interface EmployeeService {

    PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto);

    AllEmployeeDetailsResponseDto hireEmployee(HireEmployeeRequestDto hireEmployeeRequestDto);

    void changeJobDetails(ChangeJobDetailsRequestDto changeJobDetailsRequestDto);

    EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDto requestDto);

    AllEmployeeDetailsResponseDto getEmployeeById(Integer employeeId);

    PositionResponseDTO getPositionById(Integer positionId);
}
