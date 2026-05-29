package com.example.vibe_store.service;

import com.example.vibe_store.dto.grade.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GradeService {

    GradeResponseDTO createGrade(CreateGradeRequestDTO requestDTO);

    GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDTO requestDTO);

    void assignGradeRule(AssignGradeRequestDTO requestDTO);

    GradeResponseDTO getGradeById(Integer id);

    Page<GradeResponseDTO> getAllGrades(Pageable pageable);

    GradeRuleRespondDTO  getGradeRuleById(Integer id);
}
