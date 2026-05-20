package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.sale.CreateSaleRequestDTO;
import com.example.vibe_store.dto.sale.SaleResponseDTO;
import com.example.vibe_store.entity.Sale;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.repository.SaleRepository;
import com.example.vibe_store.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.vibe_store.mapper.SaleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final EmployeeWorkHistoryRepository workHistoryRepository;

    @Transactional
    @Override
    public SaleResponseDTO createSale(CreateSaleRequestDTO requestDTO) {

        EmployeeWorkHistory workHistory = workHistoryRepository
                .findByEmployeeIdAndStoreIdAndIsActiveTrue(requestDTO.employeeId(), requestDTO.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("This employee does not have an active work history for this store"));

        Sale sale = saleMapper.toEntity(requestDTO);
        sale.setStore(workHistory.getStore());
        sale.setEmployee(workHistory.getEmployee());
        Sale savedSale = saleRepository.save(sale);
        log.info("New sale created with ID: {}", savedSale.getId());

        return saleMapper.toResponse(savedSale);
    }
}