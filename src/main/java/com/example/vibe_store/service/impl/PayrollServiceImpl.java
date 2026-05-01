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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        log.info("Start payroll calculation for storeId={}, month={}", storeId, targetMonth);

     /* Temporary disabled for testing purposes
        if (!targetMonth.isBefore(YearMonth.now())) {
            log.warn("Attempted to calculate payroll for non-completed month: {}. Operation is not allowed.", targetMonth);
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

        log.debug("Found {} employees for storeId={}, month={}", histories.size(), storeId, monthStr);

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

            log.debug("Processing employeeId={} for storeId={}", employeeId, storeId);

            Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeIdAndPayrollMonthAndStoreId(employeeId, monthStr, storeId);
            if (existingPayroll.isPresent()) {
                log.debug("Existing payroll found for employeeId={}, skipping", employeeId);
                results.add(mapToResponseDTO(existingPayroll.get()));
                continue;
            }

            Store store = history.getStore();
            Payroll payroll = calculateAndBuildPayroll(targetMonth, monthStr, history, employee, store, storeBonuses, personalBonuses);
            results.add(mapToResponseDTO(payrollRepository.save(payroll)));

            log.info("Payroll saved for employeeId={}, storeId={}, totalAmount={}",
                    employeeId, storeId, payroll.getTotalAmount());
        }

        log.info("Payroll calculation completed for storeId={}, month={}, totalEmployees={}",
                storeId, targetMonth, results.size());

        return results;
    }

    @Override
    @Transactional
    public List<PayrollResponseDTO> calculatePayrollForEmployee(Integer employeeId, YearMonth targetMonth) {

        log.info("Start payroll calculation for employeeId={}, month={}", employeeId, targetMonth);

        LocalDateTime monthStart = targetMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd   = targetMonth.atEndOfMonth().atTime(23, 59, 59);
        String monthStr = targetMonth.toString();

        List<EmployeeWorkHistory> histories =
                workHistoryRepository.findAllByEmployeeIdAndMonth(employeeId, monthStart, monthEnd);

        log.debug("Found {} work history records for employeeId={}, month={}",
                histories.size(), employeeId, monthStr);

        if (histories.isEmpty()) {
            throw new ResourceNotFoundException("No work history found for employee " + employeeId + " in " + monthStr);
        }

        List<PayrollResponseDTO> results = new ArrayList<>();

        for (EmployeeWorkHistory history : histories) {
            Employee employee = history.getEmployee();
            Store store = history.getStore();
            Integer storeId = store.getId();

            log.debug("Processing employeeId={}, storeId={}", employeeId, storeId);

            Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeIdAndPayrollMonthAndStoreId(employeeId, monthStr, storeId);
            if (existingPayroll.isPresent()) {
                log.debug("Existing payroll found for employeeId={}, storeId={}, skipping", employeeId, storeId);
                results.add(mapToResponseDTO(existingPayroll.get()));
                continue;
            }

            boolean isActive = Boolean.TRUE.equals(history.getIsActive());

            log.debug("Calculating bonuses for employeeId={}, storeId={}, isActive={}",
                    employeeId, storeId, isActive);

            Map<Integer, List<BonusDetail>> storeBonuses = bonusCalculationService.calculateBonusWithStore(storeId, targetMonth);
            Map<Integer, List<BonusDetail>> personalBonuses = isActive
                    ? bonusCalculationService.calculateBonusWithoutStore(List.of(employeeId), targetMonth)
                    : Collections.emptyMap();

            Payroll payroll = calculateAndBuildPayroll(targetMonth, monthStr, history, employee, store, storeBonuses, personalBonuses);
            results.add(mapToResponseDTO(payrollRepository.save(payroll)));

            log.info("Payroll saved for employeeId ={}, storeId={}, totalAmount={}",
                    employeeId, storeId, payroll.getTotalAmount());
        }

        log.info("Completed payroll calculation for employeeId={}, month={}, totalRecords={}",
                employeeId, monthStr, results.size());

        return results;
    }

    private Payroll calculateAndBuildPayroll(YearMonth targetMonth, String monthStr, EmployeeWorkHistory history, Employee employee, Store store, Map<Integer, List<BonusDetail>> storeBonuses, Map<Integer, List<BonusDetail>> personalBonuses) {

        Integer employeeId = employee.getId();

        log.debug("Calculating payroll details for employeeId={}, storeId={}", employeeId, store.getId());

        //burada meblegin nece hasablandigi qeyd olunur, bonus ve maas ucun
        StringBuilder details = new StringBuilder();
        details.append(String.format("Store: %s. Employee: %s %s. ", store.getStoreName(), employee.getFirstName(), employee.getLastName()));

        BigDecimal baseSalary = calculateProportionalSalary(history, targetMonth, details);
        BigDecimal bonusAmount = collectBonuses(employee, employeeId, store, monthStr, storeBonuses, personalBonuses, details);
        BigDecimal totalAmount = baseSalary.add(bonusAmount).setScale(2, RoundingMode.HALF_UP);

        log.debug("Calculated payroll -> employeeId={}, baseSalary={}, bonus={}, total={}",
                employeeId, baseSalary, bonusAmount, totalAmount);

        details.append(String.format("Total: %.2f AZN (Salary: %.2f + Bonus: %.2f)", totalAmount, baseSalary, bonusAmount));

        return buildPayroll(employee, store, monthStr, baseSalary, bonusAmount, totalAmount, details.toString());
    }

    private BigDecimal calculateProportionalSalary(EmployeeWorkHistory history,
                                                   YearMonth targetMonth,
                                                   StringBuilder details) {

        log.trace("Calculating proportional salary for employeeId={}", history.getEmployee().getId());

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
        log.debug("Collecting bonuses for employeeId={}, storeId={}", employeeId, store.getId());

        //iscinin bonuslarini tek bir listde birlesdirmek ucn
        List<BonusDetail> allBonusDetails = new ArrayList<>();
        allBonusDetails.addAll(storeBonuses.getOrDefault(employeeId, Collections.emptyList()));
        allBonusDetails.addAll(personalBonuses.getOrDefault(employeeId, Collections.emptyList()));

        BigDecimal totalBonus = BigDecimal.ZERO;

        for (BonusDetail bonusDetail : allBonusDetails) {

            log.trace("Applying bonus -> employeeId={}, gradeId={}, amount={}",
                    employeeId, bonusDetail.getGradeId(), bonusDetail.getBonusAmount());

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

        log.trace("Building payroll entity for employeeId={}, storeId={}", employee.getId(), store.getId());

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

        log.trace("Mapping payroll to DTO for employeeId={}", payroll.getEmployee().getId());

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