package com.example.vibe_store.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStoreRequestDto {

    @NotBlank(message = "Store adi bos ola bilmez")
    private String storeName;

    @NotBlank(message = "Store location bos ola bilmez")
    private String storeAddress;

    @NotNull(message = "Magaza hansisa anbara(WarehouseId) baglili olmalidir")
    private Integer warehouseId;
}
