package com.example.vibe_store.service;

import com.example.vibe_store.dto.payroll.PayrollResponseDTO;

import java.time.YearMonth;
import java.util.List;

public interface PayrollService {
    List<PayrollResponseDTO> calculatePayrollForStore(Integer storeId, YearMonth targetMonth);

    List<PayrollResponseDTO> calculatePayrollForEmployee(Integer employeeId, YearMonth targetMonth);
}