package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.GradeType;

import java.time.LocalDateTime;
import java.util.List;

public record GradeResponseDTO(
        Integer gradeId,
        GradeType gradeType,
        String gradeName,
        Boolean isActive,
        LocalDateTime createdAt,
        List<GradeRuleRespondDTO> rules
) {}