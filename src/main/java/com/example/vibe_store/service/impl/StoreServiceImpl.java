package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.store.CreateStoreRequestDto;
import com.example.vibe_store.dto.store.StoreResponseDto;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.Warehouse;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.StoreRepository;
import com.example.vibe_store.repository.WarehouseRepository;
import com.example.vibe_store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    @Override
    public StoreResponseDto createStore(CreateStoreRequestDto requestDto) {
        Warehouse warehouse = warehouseRepository.findById(requestDto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Göstərilən ID ilə anbar tapılmadı: " + requestDto.getWarehouseId()));
        Store store = new Store();
        modelMapper.map(requestDto, store);
        store.setWarehouse(warehouse);
        return modelMapper.map(storeRepository.save(store), StoreResponseDto.class);
    }

    @Override
    public StoreResponseDto getStoreById(Integer id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Göstərilən ID ilə mağaza tapılmadı: " + id));
        return modelMapper.map(store, StoreResponseDto.class);
    }

    @Override
    public List<StoreResponseDto> getAllStores() {
        List<Store> stores = storeRepository.findAllWithWarehouse();
        return stores.stream()
                .map(store -> modelMapper.map(store, StoreResponseDto.class))
                .toList();
    }

    @Override
    public void deleteStore(Integer id) {
        if(!storeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Silinmək üçün mağaza tapılmadı. ID: " + id);
        }
        storeRepository.deleteById(id);
    }
}
