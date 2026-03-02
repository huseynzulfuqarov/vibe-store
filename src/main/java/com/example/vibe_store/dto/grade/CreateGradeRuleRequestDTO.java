package com.example.vibe_store.dto.grade;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateGradeRuleRequestDTO {

    private Integer gradeId;

    private BigDecimal minThreshold;

    private BigDecimal maxThreshold;

    private BigDecimal fixedAmount;

    private Double percentage;

    private BigDecimal sharePercentage;
}
