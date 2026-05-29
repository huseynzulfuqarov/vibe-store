package com.example.vibe_store.repository;

import com.example.vibe_store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Integer> {

    @Query(value = "SELECT s FROM Store s JOIN FETCH s.warehouse",
           countQuery = "SELECT count(s) FROM Store s")
    Page<Store> findAllWithWarehouse(Pageable pageable);

    @Query("SELECT s FROM Store s JOIN FETCH s.warehouse WHERE s.id = :id")
    Optional<Store> findByIdWithWarehouse(@Param("id") Integer id);
}