package com.example.vibe_store.service.impl;

import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.grade.GradeAssignment;
import com.example.vibe_store.entity.grade.GradeRule;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import com.example.vibe_store.service.BonusCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.example.vibe_store.enums.TargetType.STORE_TOTAL_SALES;

@Service
@RequiredArgsConstructor
public class BonusCalculationServiceImpl implements BonusCalculationService {

    private final EmployeeWorkHistoryRepository workHistoryRepository;
    private final GradeAssignmentRepository gradeAssignmentRepository;
    private final SaleRepository saleRepository;
    private final GradeRuleRepository gradeRuleRepository;

    @Override
    public BigDecimal calculateBonus(Employee employee, YearMonth targetMonth, StringBuilder detailsBuilder) {

        BigDecimal totalBonusAmount = BigDecimal.ZERO;

        LocalDate startOfMonth = targetMonth.atDay(1);
        LocalDate endOfMonth = targetMonth.atEndOfMonth();

        EmployeeWorkHistory activeWork = workHistoryRepository.findByEmployeeIdAndIsActiveTrue(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active Employee Work History Not Found for this Employee Id"));
        Store activeStore = activeWork.getStore();

        GradeAssignment activeGrade = gradeAssignmentRepository.findByStoreIdAndIsActiveTrue(activeStore.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Active Grade Not Found for this Store Id"));

        BigDecimal storeTotalSales = saleRepository.getTotalSalesByStoreAndDate(activeStore.getId(), startOfMonth, endOfMonth);

        if (storeTotalSales == null || storeTotalSales.compareTo(BigDecimal.ZERO) == 0) {
            return totalBonusAmount;
        }

        List<GradeRule> gradeRules = gradeRuleRepository.findAllByGradeId(activeGrade.getId());
        if (gradeRules.isEmpty()) {
            return totalBonusAmount;
        }

        BigDecimal employeeTotalSales = saleRepository.getTotalSalesByEmployeeAndDate(employee.getId(), startOfMonth, endOfMonth);
        if (employeeTotalSales == null) {
            employeeTotalSales = BigDecimal.ZERO;
        }

        for (GradeRule gradeRule : gradeRules) {
            if (STORE_TOTAL_SALES.equals(gradeRule.getTargetType()) &&
                    storeTotalSales.compareTo(gradeRule.getMinThreshold()) >= 0) {

                if (employeeTotalSales.compareTo(BigDecimal.ZERO) > 0) {


                    detailsBuilder.append("Bonus verildi. Mağaza satışı: ").append(storeTotalSales).append("\n");
                }
            }
        }

        return totalBonusAmount;
    }
}