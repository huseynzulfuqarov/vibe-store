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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public StoreResponseDTO createStore(CreateStoreRequestDTO requestDto) {
        Warehouse warehouse = warehouseRepository.findById(requestDto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + requestDto.getWarehouseId()));
        Store store = new Store();
        modelMapper.map(requestDto, store);
        store.setWarehouse(warehouse);
        return modelMapper.map(storeRepository.save(store), StoreResponseDTO.class);
    }

    @Override
    public StoreResponseDTO getStoreById(Integer id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + id));
        return modelMapper.map(store, StoreResponseDTO.class);
    }

    @Override
    public List<StoreResponseDTO> getAllStores() {
        List<Store> stores = storeRepository.findAllWithWarehouse();
        return stores.stream()
                .map(store -> modelMapper.map(store, StoreResponseDTO.class))
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