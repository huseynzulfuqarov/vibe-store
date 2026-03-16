package com.example.vibe_store.service;

import com.example.vibe_store.dto.grade.*;

public interface GradeService {

    GradeResponseDTO createGrade(CreateGradeRequestDTO requestDTO);

    GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDTO requestDTO);

    void assignGradeRule(AssignGradeRequestDTO requestDTO);

    GradeResponseDTO getGradeById(Integer id);

    GradeRuleRespondDTO  getGradeRuleById(Integer id);
}
