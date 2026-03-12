package com.example.vibe_store.service;

import com.example.vibe_store.dto.payroll.BonusDetail;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface BonusCalculationService {

        Map<Integer, List<BonusDetail>> calculateBonusWithStore(Integer storeId, YearMonth targetMonth);

        Map<Integer, List<BonusDetail>> calculateBonusWithoutStore(List<Integer> employeeIds, YearMonth targetMonth);
}