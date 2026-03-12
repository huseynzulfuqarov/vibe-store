package com.example.vibe_store.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class BonusDetail {
    private Integer gradeId;
    private String gradeName;
    private BigDecimal bonusAmount;
}
