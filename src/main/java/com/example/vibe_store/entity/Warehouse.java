package com.example.vibe_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "warehouses")

public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Company company;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "warehouse")
    List<Store> stores;

    @Column(length = 50)
    private String location;

    @CreationTimestamp
    private Date creationDate;

    @UpdateTimestamp
    private Date lastUpdate;
}
