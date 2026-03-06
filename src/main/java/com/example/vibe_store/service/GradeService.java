package com.example.vibe_store.service;

import com.example.vibe_store.dto.grade.*;

public interface GradeService {

    GradeRespondDTO createGrade(CreateGradeRequestDTO requestDTO);

    GradeRuleRespondDTO createGradeRule(Integer gradeId, CreateGradeRuleRequestDto requestDTO);

    void assignGradeRule(AssignGradeRequestDto requestDTO);

    GradeRespondDTO getGradeById(Integer id);

    GradeRuleRespondDTO  getGradeRuleById(Integer id);
}
