package com.example.vibe_store.controller;

import com.example.vibe_store.dto.payroll.PayrollResponseDTO;
import com.example.vibe_store.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @ownerChecker.isStoreManager(#storeId, authentication))")
    @PostMapping("/store/{storeId}/calculate")
    public ResponseEntity<List<PayrollResponseDTO>> calculatePayrollForStore(
            @PathVariable Integer storeId,
            @RequestParam String yearMonth) {
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        return ResponseEntity.ok(payrollService.calculatePayrollForStore(storeId, targetMonth));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/employee/{employeeId}/calculate")
    public ResponseEntity<List<PayrollResponseDTO>> calculatePayrollForEmployee(
            @PathVariable Integer employeeId,
            @RequestParam String yearMonth) {
        YearMonth targetMonth = YearMonth.parse(yearMonth);
        return ResponseEntity.ok(payrollService.calculatePayrollForEmployee(employeeId, targetMonth));
    }
}