package com.example.vibe_store.repository;

import com.example.vibe_store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    @Query("SELECT s FROM Store s JOIN FETCH s.warehouse")
    List<Store> findAllWithWarehouse();
}