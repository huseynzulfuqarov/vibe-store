package com.example.vibe_store.entity.employee;

import com.example.vibe_store.entity.grade.Grade;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employee_monthly_bonuses")
public class EmployeeMonthlyBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Grade grade;

    @Column(nullable = false)
    private BigDecimal bonusAmount;

    @CreationTimestamp
    private LocalDate calculationTime;
}
