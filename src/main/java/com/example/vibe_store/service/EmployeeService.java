package com.example.vibe_store.service;

import com.example.vibe_store.dto.employee.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto);

    HireEmployeeResponseDTO hireEmployee(HireEmployeeRequestDTO hireEmployeeRequestDto);

    AllEmployeeDetailsResponseDTO changeJobDetails(ChangeJobDetailsRequestDTO changeJobDetailsRequestDto);

    EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto);

    AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId);

    Page<AllEmployeeDetailsResponseDTO> getAllEmployees(Pageable pageable);

    PositionResponseDTO getPositionById(Integer positionId);

    Page<PositionResponseDTO> getAllPositions(Pageable pageable);
}
