package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.TargetType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GradeRuleRespondDTO {

    private Integer gradeId;

    private BigDecimal minThreshold;

    private BigDecimal maxThreshold;

    private BigDecimal fixedAmount;

    private BigDecimal percentage;

    private BigDecimal sharePercentage;

    private String positionName;

    private TargetType targetType;
}
