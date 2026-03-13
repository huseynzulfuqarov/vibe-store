package com.example.vibe_store.controller;

import com.example.vibe_store.dto.employee.*;
import com.example.vibe_store.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AllEmployeeDetailsResponseDto> hireEmployee(@RequestBody @Valid HireEmployeeRequestDto requestDto) {
        return new ResponseEntity<>(employeeService.hireEmployee(requestDto), HttpStatus.CREATED);
    }

    @PostMapping("/changeJobDetails")
    public ResponseEntity<Void> changeJobDetails(@RequestBody @Valid ChangeJobDetailsRequestDto requestDto) {
        employeeService.changeJobDetails(requestDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<EmployeeProfileResponseDTO> updateProfile(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEmployeeProfileRequestDto requestDto) {
        return new ResponseEntity<>(employeeService.updateEmployeeProfile(id, requestDto), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllEmployeeDetailsResponseDto> getEmployeeById(@PathVariable Integer id) {
        return new  ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionResponseDTO> getPosition(@PathVariable Integer id) {
        return new ResponseEntity<>(employeeService.getPositionById(id), HttpStatus.OK);
    }
}