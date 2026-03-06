package com.example.vibe_store.service;

import com.example.vibe_store.entity.employee.Employee;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface BonusCalculationService {
        BigDecimal calculateBonus(Employee employee, YearMonth targetMonth, StringBuilder detailsBuilder);
}
