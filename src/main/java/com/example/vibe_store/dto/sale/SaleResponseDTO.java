package com.example.vibe_store.dto.sale;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SaleResponseDTO {

    private Integer saleId;
    private Integer employeeId;
    private Integer storeId;
    private BigDecimal saleAmount;
    private LocalDateTime saleDate;
}
