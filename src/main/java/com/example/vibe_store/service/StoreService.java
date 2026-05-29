package com.example.vibe_store.service;

import com.example.vibe_store.dto.store.CreateStoreRequestDTO;
import com.example.vibe_store.dto.store.StoreResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreService {

    StoreResponseDTO createStore(CreateStoreRequestDTO requestDto);
    StoreResponseDTO getStoreById(Integer id);
    Page<StoreResponseDTO> getAllStores(Pageable pageable);
    void deleteStore(Integer id);
}
