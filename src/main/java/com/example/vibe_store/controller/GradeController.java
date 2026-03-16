package com.example.vibe_store.controller;

import com.example.vibe_store.dto.grade.AssignGradeRequestDTO;
import com.example.vibe_store.dto.grade.CreateGradeRequestDTO;
import com.example.vibe_store.dto.grade.CreateGradeRuleRequestDTO;
import com.example.vibe_store.dto.grade.GradeResponseDTO;
import com.example.vibe_store.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/rules")
    public ResponseEntity<Void> createGradeRule(
            @PathVariable Integer id,
            @RequestBody @Valid CreateGradeRuleRequestDTO requestDTO) {
        gradeService.createGradeRule(id, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignGrade(@RequestBody @Valid AssignGradeRequestDTO requestDTO) {
        gradeService.assignGradeRule(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
