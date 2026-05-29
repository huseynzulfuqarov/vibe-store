package com.example.vibe_store.controller;

import com.example.vibe_store.dto.store.CreateStoreRequestDTO;
import com.example.vibe_store.dto.store.StoreResponseDTO;
import com.example.vibe_store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StoreResponseDTO> createStore(@Valid @RequestBody CreateStoreRequestDTO requestDto){
        StoreResponseDTO response = storeService.createStore(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @ownerChecker.isStoreManager(#id, authentication))")
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> getStoreById(@PathVariable Integer id){
        return new ResponseEntity<>(storeService.getStoreById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<StoreResponseDTO>> getAllStores(Pageable pageable){
        return new ResponseEntity<>(storeService.getAllStores(pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer id){
        storeService.deleteStore(id);
        return  ResponseEntity.noContent().build();
    }
}
