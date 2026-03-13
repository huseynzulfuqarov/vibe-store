package com.example.vibe_store.controller;

import com.example.vibe_store.dto.sale.CreateSaleRequestDTO;
import com.example.vibe_store.dto.sale.SaleResponseDTO;
import com.example.vibe_store.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponseDTO> createSale(@RequestBody @Valid CreateSaleRequestDTO requestDTO) {
        return new ResponseEntity<>(saleService.createSale(requestDTO), HttpStatus.CREATED);
    }
}