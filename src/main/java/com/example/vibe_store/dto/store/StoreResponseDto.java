package com.example.vibe_store.dto.store;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StoreResponseDto {

    private String name;
    private String location;
    private String warehouseName;
    private LocalDate creationDate;
}
