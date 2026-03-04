package com.example.vibe_store.service;

import com.example.vibe_store.dto.employee.CreateEmployeeRequestDto;
import com.example.vibe_store.dto.employee.EmployeeResponseDto;
import com.example.vibe_store.dto.employee.TransferEmployeeRequestDto;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(CreateEmployeeRequestDto createEmployeeRequestDto);
    void transferEmployee(TransferEmployeeRequestDto  transferEmployeeRequestDto);
}
