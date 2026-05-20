package com.example.vibe_store.dto.store;

import java.time.LocalDateTime;

public record StoreResponseDTO(
        Integer storeId,
        String storeName,
        String storeAddress,
        String warehouseName,
        LocalDateTime creationDate
) {}