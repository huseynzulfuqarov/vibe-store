package com.example.vibe_store.controller;

import com.example.vibe_store.dto.employee.CreateEmployeeRequestDto;
import com.example.vibe_store.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

 /*   @PostMapping("/create")
    public void createEmployee(@RequestBody CreateEmployeeRequestDto employeeCreateDTO) {
    }*/
}
