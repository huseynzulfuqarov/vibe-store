package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.GradeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GradeResponseDTO {
    private Integer gradeId;
    private GradeType gradeType;
    private String gradeName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<GradeRuleRespondDTO> rules;
}
