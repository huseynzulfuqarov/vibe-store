package com.example.vibe_store.controller;

import com.example.vibe_store.dto.store.CreateStoreRequestDto;
import com.example.vibe_store.dto.store.StoreResponseDto;
import com.example.vibe_store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody CreateStoreRequestDto requestDto){
        StoreResponseDto response = storeService.createStore(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDto> getStoreById(@PathVariable Integer id){
        return new ResponseEntity<>(storeService.getStoreById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<StoreResponseDto>> getAllStores(){
        return new ResponseEntity<>(storeService.getAllStores(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer id){
        storeService.deleteStore(id);
        return  ResponseEntity.noContent().build();
    }
}
