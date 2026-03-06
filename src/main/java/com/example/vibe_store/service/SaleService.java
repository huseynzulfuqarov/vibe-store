package com.example.vibe_store.service;

import com.example.vibe_store.dto.sale.CreateSaleRequestDTO;
import com.example.vibe_store.dto.sale.SaleResponseDTO;

public interface SaleService {

    SaleResponseDTO createSale(CreateSaleRequestDTO requestDTO);
}
