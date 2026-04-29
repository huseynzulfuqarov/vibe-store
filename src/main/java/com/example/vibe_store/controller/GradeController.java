package com.example.vibe_store.controller;

import com.example.vibe_store.dto.grade.*;
import com.example.vibe_store.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeResponseDTO> createGrade(@RequestBody @Valid CreateGradeRequestDTO requestDTO) {

        return new ResponseEntity<>(gradeService.createGrade(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeResponseDTO> getGrade(@PathVariable Integer id) {
        return new ResponseEntity<>(gradeService.getGradeById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<GradeResponseDTO>> getAllGrades() {
        return new ResponseEntity<>(gradeService.getAllGrades(), HttpStatus.OK);
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<GradeRuleRespondDTO> createGradeRule(
            @PathVariable Integer id,
            @RequestBody @Valid CreateGradeRuleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.createGradeRule(id, requestDTO));
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignGrade(@RequestBody @Valid AssignGradeRequestDTO requestDTO) {
        gradeService.assignGradeRule(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
