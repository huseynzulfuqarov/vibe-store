package com.example.vibe_store.dto.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateSaleRequestDTO(
        @NotNull(message = "Store ID cannot be null")
        Integer storeId,

        @NotNull(message = "Employee ID cannot be null")
        Integer employeeId,

        @NotNull(message = "Sale amount cannot be null")
        @Positive
        BigDecimal saleAmount
) {}