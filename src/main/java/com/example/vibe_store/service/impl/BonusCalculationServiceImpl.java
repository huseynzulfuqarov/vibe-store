package com.example.vibe_store.service.impl;

import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.grade.GradeAssignment;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.entity.grade.GradedEmployee;
import com.example.vibe_store.enums.GradeType;
import com.example.vibe_store.enums.TargetType;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BonusCalculationServiceImpl {

    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final GradeAssignmentRepository gradeAssignmentRepo;
    private final GradedEmployeeRepository gradedEmployeeRepo;
    private final SaleRepository saleRepo;
    private final GradeRuleRepository gradeRuleRepo;
    private final StoreRepository storeRepository;

    public Map<Integer, BigDecimal> calculateBonus(Integer storeId, YearMonth targetMonth) {

        Map<Integer, BigDecimal> employeeBonuses = new HashMap<>();

        storeRepository.findById(storeId).orElseThrow(() -> new ResourceNotFoundException("Mağaza tapılmadı: " + storeId));

        LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        List<EmployeeWorkHistory> activeHistories = employeeWorkHistoryRepository.findAllActiveByStoreId(storeId);

        if (activeHistories.isEmpty()) {
            return Collections.emptyMap();
        }

        List<GradeAssignment> assignments = gradeAssignmentRepo.findByStoreIdAndEndDateInTargetMonth(storeId, startOfMonth, endOfMonth);

        for (GradeAssignment assignment : assignments) {

            GradeType gradeType = assignment.getGrade().getGradeType();
            List<GradeRule> rules = gradeRuleRepo.findAllByGradeId(assignment.getGrade().getId());

            LocalDateTime gradeStart = assignment.getStartDate();
            LocalDateTime gradeEnd = assignment.getEndDate();

            BigDecimal storeTotalSales = saleRepo.getTotalSalesByStoreAndDate(storeId, gradeStart, gradeEnd);

            boolean storeTargetMet = false;

            if (gradeType == GradeType.GRADE_THRESHOLD) {

                for (GradeRule rule : rules) {
                    if (rule.getTargetType() == TargetType.STORE_TOTAL_SALES) {
                        if (isInRange(storeTotalSales, rule)) {
                            storeTargetMet = true;
                            break;
                        }
                    }
                }
            } else {
                // FIXED_GRADE ve PERCENT_GRADE ucun eyni mentiqdir
                storeTargetMet = true;
                for (GradeRule rule : rules) {
                    if (rule.getTargetType() == TargetType.STORE_TOTAL_SALES) {
                        if (rule.getMinThreshold() != null && storeTotalSales.compareTo(rule.getMinThreshold()) < 0) {
                            storeTargetMet = false;
                            break;
                        }
                    }
                }
            }

            if (!storeTargetMet) {
                continue;
            }

            List<GradedEmployee> gradedEmployees = gradedEmployeeRepo.findAllByGradeAssignmentId(assignment.getId());

            List<EmployeeWorkHistory> eligibleEmployees = new ArrayList<>();

            if (gradedEmployees.isEmpty()) {
                eligibleEmployees = activeHistories;
            } else {
                Set<Integer> specificIds = new HashSet<>();
                for (GradedEmployee ge : gradedEmployees) {
                    specificIds.add(ge.getEmployee().getId());
                }

                for (EmployeeWorkHistory h : activeHistories) {
                    if (specificIds.contains(h.getEmployee().getId())) {
                        eligibleEmployees.add(h);
                    }
                }
            }

            if (eligibleEmployees.isEmpty()) {
                continue;
            }

            for (GradeRule rule : rules) {

                List<EmployeeWorkHistory> targetEmployees;

                if (rule.getPosition() == null) {
                    targetEmployees = eligibleEmployees;
                } else {
                    targetEmployees = new ArrayList<>();
                    for (EmployeeWorkHistory h : eligibleEmployees) {
                        if (h.getPosition().getId().equals(rule.getPosition().getId())) {
                            targetEmployees.add(h);
                        }
                    }
                }

                if (targetEmployees.isEmpty()) continue;

                List<Integer> qualifiedIds = new ArrayList<>();

                for (EmployeeWorkHistory emp : targetEmployees) {
                    Integer empId = emp.getEmployee().getId();
                    BigDecimal salesToCheck;

                    if (rule.getTargetType() == TargetType.STORE_TOTAL_SALES) {
                        //????????????????????????????????????????????????
                        salesToCheck = storeTotalSales;
                    } else {

                        LocalDateTime effectiveStart;
                        if (gradeStart.isAfter(emp.getStartDate())) {
                            effectiveStart = gradeStart;
                        } else {
                            effectiveStart = emp.getStartDate();
                        }
                        salesToCheck = saleRepo.getTotalSalesByEmployeeStoreAndDate(empId, storeId, effectiveStart, gradeEnd);
                    }

                    if (isInRange(salesToCheck, rule)) {
                        qualifiedIds.add(empId);
                    }
                }

                if (qualifiedIds.isEmpty()) continue;

                BigDecimal bonusPerPerson = BigDecimal.ZERO;

                if (gradeType == GradeType.FIXED_GRADE) {
                    if (rule.getFixedAmount() != null) {
                        bonusPerPerson = rule.getFixedAmount();
                    }
                } else {
                    if (rule.getPercentage() != null && rule.getSharePercentage() != null) {

                        BigDecimal totalPool = storeTotalSales.multiply(rule.getPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                        BigDecimal groupPool = totalPool.multiply(rule.getSharePercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                        bonusPerPerson = groupPool.divide(BigDecimal.valueOf(qualifiedIds.size()), 2, RoundingMode.HALF_UP);
                    }
                }

                if (bonusPerPerson.compareTo(BigDecimal.ZERO) > 0) {
                    for (Integer empId : qualifiedIds) {
                        BigDecimal current = employeeBonuses.getOrDefault(empId, BigDecimal.ZERO);
                        employeeBonuses.put(empId, current.add(bonusPerPerson));
                    }
                }
            }
        }

        return employeeBonuses;
    }

    private boolean isInRange(BigDecimal sales, GradeRule rule) {
        if (rule.getMinThreshold() != null && sales.compareTo(rule.getMinThreshold()) < 0) {
            return false;
        }
        if (rule.getMaxThreshold() != null && sales.compareTo(rule.getMaxThreshold()) >= 0) {
            return false;
        }
        return true;
    }
}