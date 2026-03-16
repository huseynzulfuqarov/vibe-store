package com.example.vibe_store.service;

import com.example.vibe_store.dto.store.CreateStoreRequestDTO;
import com.example.vibe_store.dto.store.StoreResponseDTO;

import java.util.List;

public interface StoreService {

    StoreResponseDTO createStore(CreateStoreRequestDTO requestDto);
    StoreResponseDTO getStoreById(Integer id);
    List<StoreResponseDTO> getAllStores();
    void deleteStore(Integer id);
}
