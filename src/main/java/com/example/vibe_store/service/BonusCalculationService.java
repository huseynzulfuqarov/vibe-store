package com.example.vibe_store.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public interface BonusCalculationService {

        Map<Integer, BigDecimal> calculateBonusWithStore(Integer storeId, YearMonth targetMonth);

        BigDecimal calculateBonusForEmployeeWithoutStore(Integer employeeId, YearMonth targetMonth);
}