package com.example.vibe_store.dto.store;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StoreResponseDTO {

    private String storeName;
    private String storeAddress;
    private String warehouseName;
    private LocalDateTime creationDate;
}
