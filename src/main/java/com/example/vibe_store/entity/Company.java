package com.example.vibe_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String companyName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "company")
    private List<Warehouse> warehouses;

    @Column(length = 50)
    private String location;

    @CreationTimestamp
    private Date creationDate;

    @UpdateTimestamp
    private Date lastUpdate;
}
