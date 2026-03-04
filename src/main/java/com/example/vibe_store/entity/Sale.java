package com.example.vibe_store.entity;

import com.example.vibe_store.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @Column(nullable = false)
    private BigDecimal saleAmount;

    @CreationTimestamp
    private LocalDateTime salesAt;
}
