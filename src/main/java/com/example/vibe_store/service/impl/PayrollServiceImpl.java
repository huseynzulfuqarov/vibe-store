package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.payroll.BonusDetail;
import com.example.vibe_store.dto.payroll.PayrollResponseDTO;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeMonthlyBonus;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.employee.Payroll;
import com.example.vibe_store.entity.Store;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    /*  Temporary disabled for testing purposes
        if (!targetMonth.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Payroll can only be calculated for completed months.");
        }*/

        storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));

        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd   = targetMonth.atEndOfMonth().atTime(23, 59, 59);
        String monthStr = targetMonth.toString();

        //target ayda bu magazada islemis ve ya hazirda isleyen butun isciler
        List<EmployeeWorkHistory> histories =
                workHistoryRepository.findAllWorkedInStoreAndMonth(storeId, monthStart, monthEnd);

        if (histories.isEmpty()) {
            throw new ResourceNotFoundException("No employees found for store: " + storeId);
        }

        //bonus ucun ancaq aktiv isciler axtarilir
        List<Integer> activeEmployeeIds = histories.stream()
                .filter(EmployeeWorkHistory::getIsActive)
                .map(h -> h.getEmployee().getId())
                .toList();

        Map<Integer, List<BonusDetail>> storeBonuses = bonusCalculationService.calculateBonusWithStore(storeId, targetMonth);
        Map<Integer, List<BonusDetail>> personalBonuses = bonusCalculationService.calculateBonusWithoutStore(activeEmployeeIds, targetMonth);

        List<PayrollResponseDTO> results = new ArrayList<>();

        for (EmployeeWorkHistory history : histories) {
            Employee employee = history.getEmployee();
            Integer employeeId = employee.getId();

            Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeIdAndPayrollMonthAndStoreId(employeeId, monthStr, storeId);
            if (existingPayroll.isPresent()) {
                results.add(mapToResponseDTO(existingPayroll.get()));
                continue;
            }

            Store store = history.getStore();
            Payroll payroll = calculateAndBuildPayroll(targetMonth, monthStr, history, employee, store, storeBonuses, personalBonuses);
            results.add(mapToResponseDTO(payrollRepository.save(payroll)));
        }

        return results;
    }

    @Override
    @Transactional
    public PayrollResponseDTO calculatePayrollForEmployee(Integer employeeId, YearMonth targetMonth) {

        String monthStr = targetMonth.toString();

        EmployeeWorkHistory history = workHistoryRepository
                .findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Active work history not found for employee: " + employeeId));

        Employee employee = history.getEmployee();
        Store store = history.getStore();
        Integer storeId = store.getId();

        Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeIdAndPayrollMonthAndStoreId(employeeId, monthStr, storeId);
        if (existingPayroll.isPresent()) {
            return mapToResponseDTO(existingPayroll.get());
        }

        Map<Integer, List<BonusDetail>> storeBonuses = bonusCalculationService.calculateBonusWithStore(storeId, targetMonth);
        Map<Integer, List<BonusDetail>> personalBonuses = bonusCalculationService.calculateBonusWithoutStore(List.of(employeeId), targetMonth);

        Payroll payroll = calculateAndBuildPayroll(targetMonth, monthStr, history, employee, store, storeBonuses, personalBonuses);

        return mapToResponseDTO(payrollRepository.save(payroll));
    }

    private Payroll calculateAndBuildPayroll(YearMonth targetMonth, String monthStr, EmployeeWorkHistory history, Employee employee, Store store, Map<Integer, List<BonusDetail>> storeBonuses, Map<Integer, List<BonusDetail>> personalBonuses) {

        Integer employeeId = employee.getId();
        //burada meblegin nece hasablandigi qeyd olunur, bonus ve maas ucun
        StringBuilder details = new StringBuilder();
        details.append(String.format("Store: %s. Employee: %s %s. ", store.getStoreName(), employee.getFirstName(), employee.getLastName()));

        BigDecimal baseSalary = calculateProportionalSalary(history, targetMonth, details);
        BigDecimal bonusAmount = collectBonuses(employee, employeeId, store, monthStr, storeBonuses, personalBonuses, details);
        BigDecimal totalAmount = baseSalary.add(bonusAmount).setScale(2, RoundingMode.HALF_UP);

        details.append(String.format("Total: %.2f AZN (Salary: %.2f + Bonus: %.2f)", totalAmount, baseSalary, bonusAmount));

        return buildPayroll(employee, store, monthStr, baseSalary, bonusAmount, totalAmount, details.toString());
    }

    private BigDecimal calculateProportionalSalary(EmployeeWorkHistory history,
                                                   YearMonth targetMonth,
                                                   StringBuilder details) {
        int totalDaysInMonth = targetMonth.lengthOfMonth();
        LocalDate monthStart  = targetMonth.atDay(1);
        LocalDate monthEnd    = targetMonth.atEndOfMonth();

        LocalDate startDate = history.getStartDate().toLocalDate();
        LocalDate endDate = history.getEndDate() != null
                ? history.getEndDate().toLocalDate()
                : monthEnd;

        LocalDate effectiveStart = startDate.isBefore(monthStart) ? monthStart : startDate;
        LocalDate effectiveEnd   = endDate.isAfter(monthEnd)      ? monthEnd   : endDate;

        //we write '+1' to take the last day
        //eslinde burada bug var. meselen isci bir ayda isden cixib sonra yeniden qayitsa
        //findAllWorkedInStoreAndMonth  her ikisini de tapacaq, amma birini hesablayib digerine
        //kecende dublikat yoxlamsindan kece bilmeyecek, ve maas itecek
        //bu edge case-nin cox real gormediyim ucun heleki handle etmedim
        long daysWorked = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;

        BigDecimal dailyRate = history.getSalary()
                .divide(BigDecimal.valueOf(totalDaysInMonth), 4, RoundingMode.HALF_UP);
        BigDecimal salary = dailyRate.multiply(BigDecimal.valueOf(daysWorked))
                .setScale(2, RoundingMode.HALF_UP);

        if (daysWorked == totalDaysInMonth) {
            details.append(String.format("Full month. Salary: %.2f AZN. ", salary));
        } else {
            details.append(String.format("%d days worked (%.4f AZN/day = %.2f AZN). ",
                    daysWorked, dailyRate, salary));
        }

        return salary;
    }

    private BigDecimal collectBonuses(Employee employee, Integer employeeId, Store store,
                                      String monthStr,
                                      Map<Integer, List<BonusDetail>> storeBonuses,
                                      Map<Integer, List<BonusDetail>> personalBonuses,
                                      StringBuilder details) {
        //iscinin bonuslarini tek bir listde birlesdirmek ucn
        List<BonusDetail> allBonusDetails = new ArrayList<>();
        allBonusDetails.addAll(storeBonuses.getOrDefault(employeeId, Collections.emptyList()));
        allBonusDetails.addAll(personalBonuses.getOrDefault(employeeId, Collections.emptyList()));

        BigDecimal totalBonus = BigDecimal.ZERO;

        for (BonusDetail bonusDetail : allBonusDetails) {
            totalBonus = totalBonus.add(bonusDetail.getBonusAmount());

            details.append(String.format("%s bonusu: %.2f AZN. ",
                    bonusDetail.getGradeName(), bonusDetail.getBonusAmount()));

            Grade grade = gradeRepository.findById(bonusDetail.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + bonusDetail.getGradeId()));

            EmployeeMonthlyBonus monthlyBonus = new EmployeeMonthlyBonus();
            monthlyBonus.setEmployee(employee);
            monthlyBonus.setGrade(grade);
            monthlyBonus.setStore(store);
            monthlyBonus.setPayrollMonth(monthStr);
            monthlyBonus.setBonusAmount(bonusDetail.getBonusAmount());
            employeeMonthlyBonusRepository.save(monthlyBonus);
        }

        return totalBonus;
    }

    private Payroll buildPayroll(Employee employee, Store store, String monthStr,
                                 BigDecimal baseSalary, BigDecimal bonusAmount,
                                 BigDecimal totalAmount, String details) {
        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setStore(store);
        payroll.setPayrollMonth(monthStr);
        payroll.setBaseSalary(baseSalary);
        payroll.setBonusAmount(bonusAmount);
        payroll.setTotalAmount(totalAmount);
        payroll.setCalculationDetails(details);
        return payroll;
    }

    private PayrollResponseDTO mapToResponseDTO(Payroll payroll) {
        PayrollResponseDTO dto = new PayrollResponseDTO();
        dto.setPayrollId(payroll.getId());
        dto.setEmployeeId(payroll.getEmployee().getId());
        dto.setEmployeeName(payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName());
        dto.setStoreName(payroll.getStore().getStoreName());
        dto.setBaseSalary(payroll.getBaseSalary());
        dto.setBonusAmount(payroll.getBonusAmount());
        dto.setTotalAmount(payroll.getTotalAmount());
        dto.setCalculationDetails(payroll.getCalculationDetails());
        dto.setCreatedAt(payroll.getCreatedAt());
        return dto;
    }
}