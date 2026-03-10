package com.example.vibe_store.repository;

import com.example.vibe_store.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    BigDecimal getTotalSalesByEmployeeStoreAndDate(Integer empId, Integer storeId, LocalDateTime effectiveStart,LocalDateTime gradeEnd);

    @Query("SELECT COALESCE(SUM(s.saleAmount), 0) FROM Sale s WHERE s.store.id = :storeId AND s.salesAt >= :startDate AND s.salesAt <= :endDate")
    BigDecimal getTotalSalesByStoreAndDate(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}