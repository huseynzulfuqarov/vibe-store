package com.example.vibe_store.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoreRequestDTO(
        @NotBlank(message = "Store name cannot be blank")
        String storeName,

        @NotBlank(message = "Store address cannot be blank")
        String storeAddress,

        @NotNull(message = "Store must be linked to a warehouse (warehouseId)")
        Integer warehouseId
) {}