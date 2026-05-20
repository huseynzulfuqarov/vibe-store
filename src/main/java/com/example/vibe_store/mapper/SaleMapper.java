package com.example.vibe_store.mapper;

import com.example.vibe_store.dto.sale.CreateSaleRequestDTO;
import com.example.vibe_store.dto.sale.SaleResponseDTO;
import com.example.vibe_store.entity.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(source = "id", target = "saleId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "salesAt", target = "saleDate")
    SaleResponseDTO toResponse(Sale sale);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "salesAt", ignore = true)
    Sale toEntity(CreateSaleRequestDTO request);
}