package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.sale.CreateSaleRequestDTO;
import com.example.vibe_store.dto.sale.SaleResponseDTO;
import com.example.vibe_store.entity.Sale;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.repository.SaleRepository;
import com.example.vibe_store.repository.StoreRepository;
import com.example.vibe_store.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final ModelMapper modelMapper;
    private final EmployeeWorkHistoryRepository workHistoryRepository;

    @Override
    public SaleResponseDTO createSale(CreateSaleRequestDTO requestDTO) {

        Store store = storeRepository.findById(requestDTO.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + requestDTO.getStoreId()));

        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + requestDTO.getEmployeeId()));

        workHistoryRepository.findByEmployeeIdAndStoreIdAndIsActiveTrue(requestDTO.getEmployeeId(), requestDTO.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("This employee does not have an active work history for this store"));

        Sale sale = modelMapper.map(requestDTO, Sale.class);
        sale.setStore(store);
        sale.setEmployee(employee);
        Sale savedSale = saleRepository.save(sale);

        SaleResponseDTO responseDTO = modelMapper.map(savedSale, SaleResponseDTO.class);
        responseDTO.setEmployeeId(savedSale.getEmployee().getId());
        responseDTO.setStoreId(savedSale.getStore().getId());
        responseDTO.setSaleId(savedSale.getId());
        responseDTO.setSaleDate(savedSale.getSalesAt());

        return responseDTO;
    }
}