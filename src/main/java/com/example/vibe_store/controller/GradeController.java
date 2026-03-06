package com.example.vibe_store.controller;

import com.example.vibe_store.dto.grade.AssignGradeRequestDto;
import com.example.vibe_store.dto.grade.CreateGradeRequestDTO;
import com.example.vibe_store.dto.grade.CreateGradeRuleRequestDto;
import com.example.vibe_store.dto.grade.GradeRespondDTO;
import com.example.vibe_store.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/grades")
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<Void> createGrade(@RequestBody @Valid CreateGradeRequestDTO requestDTO) {
        gradeService.createGrade(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeRespondDTO> getGrade(@PathVariable Integer id) {
       return new ResponseEntity<>(gradeService.getGradeById(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<Void> createGradeRule(
            @PathVariable Integer id,
            @RequestBody @Valid CreateGradeRuleRequestDto requestDTO) {
        gradeService.createGradeRule(id, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignGrade(@RequestBody @Valid AssignGradeRequestDto requestDTO) {
        gradeService.assignGradeRule(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
