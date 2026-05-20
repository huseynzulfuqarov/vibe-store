package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.store.CreateStoreRequestDTO;
import com.example.vibe_store.dto.store.StoreResponseDTO;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.Warehouse;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.StoreRepository;
import com.example.vibe_store.repository.WarehouseRepository;
import com.example.vibe_store.service.StoreService;
import lombok.RequiredArgsConstructor;
import com.example.vibe_store.mapper.StoreMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreMapper storeMapper;

    @Transactional
    @Override
    public StoreResponseDTO createStore(CreateStoreRequestDTO requestDto) {
        Warehouse warehouse = warehouseRepository.findById(requestDto.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + requestDto.warehouseId()));
        Store store = storeMapper.toEntity(requestDto);
        store.setWarehouse(warehouse);
        return storeMapper.toResponse(storeRepository.save(store));
    }

    @Override
    public StoreResponseDTO getStoreById(Integer id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + id));
        return storeMapper.toResponse(store);
    }

    @Override
    public List<StoreResponseDTO> getAllStores() {
        List<Store> stores = storeRepository.findAllWithWarehouse();
        return stores.stream()
                .map(storeMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteStore(Integer id) {
        if (!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Store not found for deletion. ID: " + id);
        }
        storeRepository.deleteById(id);
    }
}