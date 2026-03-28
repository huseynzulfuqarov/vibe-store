package com.example.vibe_store.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStoreRequestDTO {

    @NotBlank(message = "Store name cannot be blank")
    private String storeName;

    @NotBlank(message = "Store address cannot be blank")
    private String storeAddress;

    @NotNull(message = "Store must be linked to a warehouse (warehouseId)")
    private Integer warehouseId;
}