package com.example.vibe_store.controller;

import com.example.vibe_store.dto.employee.*;
import com.example.vibe_store.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/positions")
    public ResponseEntity<PositionResponseDTO> createPosition(@RequestBody @Valid CreatePositionRequestDTO requestDto) {
        return new ResponseEntity<>(employeeService.createPosition(requestDto), HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<AllEmployeeDetailsResponseDTO> hireEmployee(@RequestBody @Valid HireEmployeeRequestDTO requestDto) {
        return new ResponseEntity<>(employeeService.hireEmployee(requestDto), HttpStatus.CREATED);
    }

    @PostMapping("/changeJobDetails")
    public ResponseEntity<AllEmployeeDetailsResponseDTO> changeJobDetails(@RequestBody @Valid ChangeJobDetailsRequestDTO requestDto) {
        return ResponseEntity.ok(employeeService.changeJobDetails(requestDto));
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<EmployeeProfileResponseDTO> updateProfile(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeProfileRequestDTO requestDto) {
        return new ResponseEntity<>(employeeService.updateEmployeeProfile(id, requestDto), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllEmployeeDetailsResponseDTO> getEmployeeById(@PathVariable Integer id) {
        return new  ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AllEmployeeDetailsResponseDTO>> getAllEmployees() {
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionResponseDTO> getPosition(@PathVariable Integer id) {
        return new ResponseEntity<>(employeeService.getPositionById(id), HttpStatus.OK);
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponseDTO>> getAllPositions() {
        return new ResponseEntity<>(employeeService.getAllPositions(), HttpStatus.OK);
    }
}