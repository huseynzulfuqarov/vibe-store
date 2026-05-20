package com.example.vibe_store.mapper;

import com.example.vibe_store.dto.store.CreateStoreRequestDTO;
import com.example.vibe_store.dto.store.StoreResponseDTO;
import com.example.vibe_store.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(source = "id", target = "storeId")
    @Mapping(source = "warehouse.warehouseName", target = "warehouseName")
    StoreResponseDTO toResponse(Store store);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    Store toEntity(CreateStoreRequestDTO request);
}