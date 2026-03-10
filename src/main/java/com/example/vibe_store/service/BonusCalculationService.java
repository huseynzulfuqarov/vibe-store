package com.example.vibe_store.service;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface BonusCalculationService {
        BigDecimal calculateBonusForEmployee(Integer employeeId, YearMonth targetMonth, StringBuilder detailsBuilder);
}