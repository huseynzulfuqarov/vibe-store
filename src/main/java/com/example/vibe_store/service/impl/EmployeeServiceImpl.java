package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.EmployeeCreateDTO;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final ModelMapper modelMapper;

    public void createEmployee(EmployeeCreateDTO employeeCreateDTO) {
        Employee employee = new Employee();
        modelMapper.map(employeeCreateDTO, employee);
        employeeRepository.save(employee);
    }
}
