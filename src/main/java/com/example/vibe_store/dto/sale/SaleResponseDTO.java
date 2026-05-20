package com.example.vibe_store.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponseDTO(
        Long saleId,
        Integer employeeId,
        Integer storeId,
        BigDecimal saleAmount,
        LocalDateTime saleDate
) {}