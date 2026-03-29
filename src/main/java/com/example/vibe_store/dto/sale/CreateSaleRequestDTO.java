package com.example.vibe_store.dto.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSaleRequestDTO {

    @NotNull(message = "Store ID cannot be null")
    private Integer storeId;

    @NotNull(message = "Employee ID cannot be null")
    private Integer employeeId;

    @NotNull(message = "Sale amount cannot be null")
    @Positive
    private BigDecimal saleAmount;
}