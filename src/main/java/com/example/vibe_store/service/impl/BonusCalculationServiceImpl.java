package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.payroll.BonusDetail;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.grade.GradeAssignment;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.entity.grade.GradedEmployee;
import com.example.vibe_store.enums.GradeType;
import com.example.vibe_store.enums.TargetType;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import com.example.vibe_store.service.BonusCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BonusCalculationServiceImpl implements BonusCalculationService {

    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final GradeAssignmentRepository gradeAssignmentRepo;
    private final GradedEmployeeRepository gradedEmployeeRepo;
    private final SaleRepository saleRepo;
    private final GradeRuleRepository gradeRuleRepo;

    @Override
    public Map<Integer, List<BonusDetail>> calculateBonusWithStore(Integer storeId, YearMonth targetMonth) {

        LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        List<EmployeeWorkHistory> activeHistories = employeeWorkHistoryRepository.findAllActiveByStoreId(storeId);

        if (activeHistories.isEmpty()) {
            return Collections.emptyMap();
        }

        // if employees exist, create a map for bonuses
        Map<Integer, List<BonusDetail>> employeeBonuses = new HashMap<>();

        List<GradeAssignment> assignments = gradeAssignmentRepo
                .findByStoreIdAndEndDateInTargetMonth(storeId, startOfMonth, endOfMonth);

        for (GradeAssignment assignment : assignments) {
          processStoreAssignment(assignment, storeId, activeHistories, employeeBonuses);
        }

      return employeeBonuses;
    }

    @Override
    public Map<Integer, List<BonusDetail>> calculateBonusWithoutStore(List<Integer> employeeIds, YearMonth targetMonth) {

      LocalDateTime startOfMonth = targetMonth.atDay(1).atStartOfDay();
      LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);

      Map<Integer, List<BonusDetail>> employeeBonuses = new HashMap<>();

      for (Integer employeeId : employeeIds){
          List<GradeAssignment> assignments = gradeAssignmentRepo
                  .findStoreNullByEmployeeAndMonth(employeeId, startOfMonth, endOfMonth);

          if (assignments.isEmpty()) continue;

          EmployeeWorkHistory activeHistory = employeeWorkHistoryRepository
                  .findByEmployeeIdAndIsActiveTrue(employeeId)
                  .orElseThrow(() -> new ResourceNotFoundException("Active work history not found for employee: " + employeeId));

          for (GradeAssignment assignment : assignments) {
            processIndividualAssignment(assignment, employeeId, activeHistory, employeeBonuses);
          }
      }

      return employeeBonuses;
    }

    private void processStoreAssignment(GradeAssignment assignment, Integer storeId,
                                        List<EmployeeWorkHistory> activeHistories,
                                        Map<Integer, List<BonusDetail>> employeeBonuses) {

        GradeType gradeType = assignment.getGrade().getGradeType();
        List<GradeRule> rules = gradeRuleRepo.findAllByGradeId(assignment.getGrade().getId());

        LocalDateTime gradeStart = assignment.getStartDate();
        LocalDateTime gradeEnd = assignment.getEndDate();

        BigDecimal storeTotalSales = saleRepo.getTotalSalesByStoreAndDate(storeId, gradeStart, gradeEnd);

        if (!isStoreTargetMet(rules, storeTotalSales)) return;

        List<GradedEmployee> gradedEmployees = gradedEmployeeRepo.findAllByGradeAssignmentId(assignment.getId());
        List<EmployeeWorkHistory> eligibleEmployees = getEligibleEmployees(gradedEmployees, activeHistories);

        if (eligibleEmployees.isEmpty()) return;

        for (GradeRule rule : rules) {
          processStoreRule(rule, gradeType, eligibleEmployees, storeId, storeTotalSales, assignment, gradeStart, gradeEnd, employeeBonuses);
        }
    }

    private void processStoreRule(GradeRule rule, GradeType gradeType,
                                  List<EmployeeWorkHistory> eligibleEmployees,
                                  Integer storeId, BigDecimal storeTotalSales,
                                  GradeAssignment assignment,
                                  LocalDateTime gradeStart, LocalDateTime gradeEnd,
                                  Map<Integer, List<BonusDetail>> employeeBonuses) {

        List<EmployeeWorkHistory> targetEmployees = getTargetEmployees(eligibleEmployees, rule);
        if (targetEmployees.isEmpty()) return;

        List<Integer> qualifiedIds = new ArrayList<>();
        for (EmployeeWorkHistory emp : targetEmployees) {

            Integer empId = emp.getEmployee().getId();
            BigDecimal salesToCheck;

            // For GRADE_THRESHOLD, we must check this because even though the store limit was passed,
            // we need to verify if the limit for this specific rule within that grade type was met.
            if (rule.getTargetType() == TargetType.STORE_TOTAL_SALES) {
              salesToCheck = storeTotalSales;
            } else {
              // for individual rules
              LocalDateTime effectiveStart = resolveEffectiveStart(gradeStart, emp.getStartDate());
              salesToCheck = saleRepo.getTotalSalesByEmployeeStoreAndDate(empId, storeId, effectiveStart, gradeEnd);
            }

            if (isInRange(salesToCheck, rule)) {
              qualifiedIds.add(empId);
            }
        }

        if (qualifiedIds.isEmpty()) return;

        BigDecimal bonusPerPerson = calculateStoreBonus(gradeType, rule, storeTotalSales, qualifiedIds.size());

        if (bonusPerPerson.compareTo(BigDecimal.ZERO) > 0) {
            for (Integer emId : qualifiedIds) {
              addBonusToMap(employeeBonuses, emId, assignment, bonusPerPerson);
            }
        }
    }

    private void processIndividualAssignment(GradeAssignment assignment, Integer employeeId,
                                             EmployeeWorkHistory activeHistory,
                                             Map<Integer, List<BonusDetail>> employeeBonuses) {

        GradeType gradeType = assignment.getGrade().getGradeType();
        List<GradeRule> rules = gradeRuleRepo.findAllByGradeId(assignment.getGrade().getId());

        LocalDateTime gradeStart = assignment.getStartDate();
        LocalDateTime gradeEnd = assignment.getEndDate();
        Integer currentStoreId = activeHistory.getStore().getId();

        for (GradeRule rule : rules) {

          // ignore STORE_TOTAL_SALES rules for individual calculation
            if (rule.getTargetType() == TargetType.STORE_TOTAL_SALES) continue;

            if (rule.getPosition() != null && !activeHistory.getPosition().getId().equals(rule.getPosition().getId()))
              continue;

            LocalDateTime effectiveStart = resolveEffectiveStart(gradeStart, activeHistory.getStartDate());
            BigDecimal employeeSales = saleRepo.getTotalSalesByEmployeeStoreAndDate(employeeId, currentStoreId, effectiveStart, gradeEnd);

            if (!isInRange(employeeSales, rule)) continue;

            BigDecimal bonus = calculateIndividualBonus(gradeType, rule, employeeSales);
            addBonusToMap(employeeBonuses, employeeId, assignment, bonus);
        }
    }

    private boolean isStoreTargetMet(List<GradeRule> rules, BigDecimal storeTotalSales) {
        List<GradeRule> storeRules = rules.stream()
                .filter(r -> r.getTargetType() == TargetType.STORE_TOTAL_SALES)
                .toList();

        // no store-level rule means no store limit — always pass
        if (storeRules.isEmpty()) return true;

        // for all grade types: at least one store rule's range must be satisfied
        return storeRules.stream().anyMatch(r -> isInRange(storeTotalSales, r));
    }

    private BigDecimal calculateStoreBonus(GradeType gradeType, GradeRule rule,
                                           BigDecimal storeTotalSales, int qualifiedCount) {
        if (gradeType == GradeType.FIXED_GRADE) {
            return rule.getFixedAmount() != null ? rule.getFixedAmount() : BigDecimal.ZERO;
        }
        if (rule.getPercentage() == null || rule.getSharePercentage() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPool = storeTotalSales
                .multiply(rule.getPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal groupPool = totalPool
                .multiply(rule.getSharePercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return groupPool.divide(BigDecimal.valueOf(qualifiedCount), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateIndividualBonus(GradeType gradeType, GradeRule rule, BigDecimal employeeSales) {
        if (gradeType == GradeType.FIXED_GRADE) {
            return rule.getFixedAmount() != null ? rule.getFixedAmount() : BigDecimal.ZERO;
        }
        if (rule.getPercentage() == null) {
            return BigDecimal.ZERO;
        }
        return employeeSales.multiply(rule.getPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private boolean isInRange(BigDecimal sales, GradeRule rule) {
        boolean satisfiesMin = rule.getMinThreshold() == null || sales.compareTo(rule.getMinThreshold()) >= 0;
        boolean satisfiesMax = rule.getMaxThreshold() == null || sales.compareTo(rule.getMaxThreshold()) < 0;
        return satisfiesMin && satisfiesMax;
    }

    private List<EmployeeWorkHistory> getEligibleEmployees(
            List<GradedEmployee> gradedEmployees,
            List<EmployeeWorkHistory> activeHistories) {

        if (gradedEmployees.isEmpty()) return activeHistories;

        // check if employee is still active (may have left the company)
        Set<Integer> specificIds = gradedEmployees.stream()
                .map(ge -> ge.getEmployee().getId())
                .collect(Collectors.toSet());

        return activeHistories.stream()
                .filter(h -> specificIds.contains(h.getEmployee().getId()))
                .toList();
    }

    private List<EmployeeWorkHistory> getTargetEmployees(
            List<EmployeeWorkHistory> eligibleEmployees,
            GradeRule rule) {

        if (rule.getPosition() == null) return eligibleEmployees;

        return eligibleEmployees.stream()
                .filter(h -> h.getPosition().getId().equals(rule.getPosition().getId()))
                .toList();
    }

    private void addBonusToMap(Map<Integer, List<BonusDetail>> bonusMap, Integer employeeId,
                               GradeAssignment assignment, BigDecimal bonusAmount) {
        if (bonusAmount.compareTo(BigDecimal.ZERO) > 0) {
            bonusMap.computeIfAbsent(employeeId, k -> new ArrayList<>())
                    .add(new BonusDetail(
                            assignment.getGrade().getId(),
                            assignment.getGrade().getGradeName(),
                            bonusAmount));
        }
    }

    private LocalDateTime resolveEffectiveStart(LocalDateTime gradeStart, LocalDateTime employeeStart) {
        if (gradeStart.isAfter(employeeStart)) {
            return gradeStart;
        }
        return employeeStart;
    }
}
