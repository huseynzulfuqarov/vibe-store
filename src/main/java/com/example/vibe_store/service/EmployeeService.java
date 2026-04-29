package com.example.vibe_store.service;

import com.example.vibe_store.dto.employee.*;

import java.util.List;

public interface EmployeeService {

    PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto);

    AllEmployeeDetailsResponseDTO hireEmployee(HireEmployeeRequestDTO hireEmployeeRequestDto);

    AllEmployeeDetailsResponseDTO changeJobDetails(ChangeJobDetailsRequestDTO changeJobDetailsRequestDto);

    EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto);

    AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId);

    List<AllEmployeeDetailsResponseDTO> getAllEmployees();

    PositionResponseDTO getPositionById(Integer positionId);

    List<PositionResponseDTO> getAllPositions();
}
