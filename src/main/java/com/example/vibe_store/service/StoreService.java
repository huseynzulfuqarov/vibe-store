package com.example.vibe_store.service;

import com.example.vibe_store.dto.store.CreateStoreRequestDto;
import com.example.vibe_store.dto.store.StoreResponseDto;

import java.util.List;

public interface StoreService {

    StoreResponseDto createStore(CreateStoreRequestDto requestDto);
    StoreResponseDto getStoreById(Integer id);
    List<StoreResponseDto> getAllStores();
    void deleteStore(Integer id);
}
