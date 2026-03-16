package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.payroll.BonusDetail;
import com.example.vibe_store.dto.payroll.PayrollResponseDTO;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeMonthlyBonus;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.employee.Payroll;
import com.example.vibe_store.entity.grade.Grade;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.*;
import com.example.vibe_store.service.BonusCalculationService;
import com.example.vibe_store.service.PayrollService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final EmployeeWorkHistoryRepository workHistoryRepository;
    private final PayrollRepository payrollRepository;
    private final StoreRepository storeRepository;
    private final GradeRepository gradeRepository;
    private final EmployeeMonthlyBonusRepository employeeMonthlyBonusRepository;
    private final BonusCalculationService bonusCalculationService;

    @Override
    @Transactional
    public List<PayrollResponseDTO> calculatePayrollForStore(Integer storeId, YearMonth targetMonth) {

        //test etmek ucun bu yoxlamani muveqqeti bagladiq

       /* if (!targetMonth.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Yalnız başa çatmış aylar üçün maaş hesablana bilər.");
        }*/

        final int STANDARD_DAYS_IN_MONTH = targetMonth.lengthOfMonth();

        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mağaza tapılmadı: " + storeId));

        String monthStr = targetMonth.toString();

        List<EmployeeWorkHistory> activeHistories = workHistoryRepository.findAllActiveByStoreId(storeId);

        if (activeHistories.isEmpty()) {
            throw new ResourceNotFoundException("Bu mağazada aktiv işçi tapılmadı: " + storeId);
        }

        List<Integer> employeeIds = activeHistories.stream()
                .map(h -> h.getEmployee().getId())
                .toList();

        Map<Integer, List<BonusDetail>> storeBonuses = bonusCalculationService.calculateBonusWithStore(storeId, targetMonth);
        Map<Integer, List<BonusDetail>> personalBonuses = bonusCalculationService.calculateBonusWithoutStore(employeeIds, targetMonth);

        List<PayrollResponseDTO> results = new ArrayList<>();

        for (EmployeeWorkHistory activeHistory : activeHistories) {
            Employee employee = activeHistory.getEmployee();
            Integer employeeId = employee.getId();

            if (payrollRepository.existsByEmployeeIdAndPayrollMonth(employeeId, monthStr)) {
                Payroll existing = payrollRepository.findByEmployeeIdAndPayrollMonth(employeeId, monthStr)
                        .orElseThrow(() -> new ResourceNotFoundException("Payroll tapılmadı"));
                results.add(mapToResponseDTO(existing));
                continue;
            }

            LocalDate monthStart = targetMonth.atDay(1);
            LocalDate monthEnd = targetMonth.atEndOfMonth();

            BigDecimal totalBaseSalary;
            StringBuilder details = new StringBuilder();

            LocalDate activeStartDate = activeHistory.getStartDate().toLocalDate();

            if (activeStartDate.isAfter(monthStart) && !activeStartDate.isAfter(monthEnd)) {

                List<EmployeeWorkHistory> allHistories = workHistoryRepository.findAllByEmployeeId(employeeId);

                BigDecimal oldSalary = BigDecimal.ZERO;
                LocalDate effectiveOldStart = monthStart;
                for (EmployeeWorkHistory h : allHistories) {
                    if (!h.getId().equals(activeHistory.getId())
                            && h.getEndDate() != null
                            && !h.getEndDate().toLocalDate().isBefore(monthStart)) {
                        oldSalary = h.getSalary();
                        LocalDate oldHistoryStart = h.getStartDate().toLocalDate();
                        effectiveOldStart = oldHistoryStart.isAfter(monthStart) ? oldHistoryStart : monthStart;
                        break;
                    }
                }

                long daysAtOldStore = ChronoUnit.DAYS.between(effectiveOldStart, activeStartDate);
                long daysAtNewStore = ChronoUnit.DAYS.between(activeStartDate,
                        targetMonth.atEndOfMonth().plusDays(1));

                BigDecimal dailyOld = oldSalary.divide(BigDecimal.valueOf(STANDARD_DAYS_IN_MONTH), 2, RoundingMode.HALF_UP);
                BigDecimal salaryOldPart = dailyOld.multiply(BigDecimal.valueOf(daysAtOldStore));

                BigDecimal dailyNew = activeHistory.getSalary().divide(BigDecimal.valueOf(STANDARD_DAYS_IN_MONTH), 2, RoundingMode.HALF_UP);
                BigDecimal salaryNewPart = dailyNew.multiply(BigDecimal.valueOf(daysAtNewStore));

                totalBaseSalary = salaryOldPart.add(salaryNewPart).setScale(2, RoundingMode.HALF_UP);

                if (daysAtOldStore == 0) {
                    details.append(String.format("Bu ay işə götürülüb və dərhal transfer edilib. " +
                                    "Yeni mağazada %d gün (%.2f AZN/gün = %.2f AZN). ",
                            daysAtNewStore, dailyNew, salaryNewPart));
                } else {
                    details.append(String.format("Transfer olunub. Köhnə mağazada %d gün (%.2f AZN/gün = %.2f AZN), ",
                            daysAtOldStore, dailyOld, salaryOldPart));
                    details.append(String.format("Yeni mağazada %d gün (%.2f AZN/gün = %.2f AZN). ",
                            daysAtNewStore, dailyNew, salaryNewPart));
                }

            } else if (activeStartDate.isAfter(monthEnd)) {
                continue;

            } else {
                totalBaseSalary = activeHistory.getSalary();
                details.append(String.format("Tam ay işləyib. Maaş: %.2f AZN. ", totalBaseSalary));
            }

            BigDecimal bonusAmount = BigDecimal.ZERO;

            List<BonusDetail> allBonusDetails = new ArrayList<>();
            allBonusDetails.addAll(storeBonuses.getOrDefault(employeeId, Collections.emptyList()));
            allBonusDetails.addAll(personalBonuses.getOrDefault(employeeId, Collections.emptyList()));

            for (BonusDetail bonusDetail : allBonusDetails) {
                bonusAmount = bonusAmount.add(bonusDetail.getBonusAmount());

                details.append(String.format("%s bonusu: %.2f AZN. ",
                        bonusDetail.getGradeName(), bonusDetail.getBonusAmount()));

                Grade grade = gradeRepository.findById(bonusDetail.getGradeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Grade tapılmadı: " + bonusDetail.getGradeId()));

                EmployeeMonthlyBonus monthlyBonus = new EmployeeMonthlyBonus();
                monthlyBonus.setEmployee(employee);
                monthlyBonus.setGrade(grade);
                monthlyBonus.setPayrollMonth(monthStr);
                monthlyBonus.setBonusAmount(bonusDetail.getBonusAmount());
                employeeMonthlyBonusRepository.save(monthlyBonus);
            }

            BigDecimal totalAmount = totalBaseSalary.add(bonusAmount).setScale(2, RoundingMode.HALF_UP);

            details.append(String.format("Ümumi: %.2f AZN (Maaş: %.2f + Bonus: %.2f)",
                    totalAmount, totalBaseSalary, bonusAmount));

            Payroll payroll = new Payroll();
            payroll.setEmployee(employee);
            payroll.setPayrollMonth(monthStr);
            payroll.setBaseSalary(totalBaseSalary);
            payroll.setBonusAmount(bonusAmount);
            payroll.setTotalAmount(totalAmount);
            payroll.setCalculationDetails(details.toString());

            Payroll savedPayroll = payrollRepository.save(payroll);
            results.add(mapToResponseDTO(savedPayroll));
        }

        return results;
    }

    private PayrollResponseDTO mapToResponseDTO(Payroll payroll) {
        PayrollResponseDTO dto = new PayrollResponseDTO();
        dto.setPayrollId(payroll.getId());
        dto.setEmployeeId(payroll.getEmployee().getId());
        dto.setEmployeeName(payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName());
        dto.setBaseSalary(payroll.getBaseSalary());
        dto.setBonusAmount(payroll.getBonusAmount());
        dto.setTotalAmount(payroll.getTotalAmount());
        dto.setCalculationDetails(payroll.getCalculationDetails());
        dto.setCreatedAt(payroll.getCreatedAt());
        return dto;
    }
}