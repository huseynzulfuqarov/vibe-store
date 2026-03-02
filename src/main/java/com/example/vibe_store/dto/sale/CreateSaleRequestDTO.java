package com.example.vibe_store.dto.sale;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSaleRequestDTO {

    @NotNull(message = "@NotNull(bos ola bilmez")
    private Integer storeId;

    @NotNull(message = "bos ola bilmez")
    private Integer employeeId;

    @NotNull(message = "bos ola bilmez")
    private BigDecimal saleAmount;
}
