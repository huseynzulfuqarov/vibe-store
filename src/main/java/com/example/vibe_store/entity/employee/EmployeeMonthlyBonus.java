package com.example.vibe_store.entity.employee;

import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.grade.Grade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "employee_monthly_bonuses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "grade_id", "payroll_month", "store_id"}))
public class EmployeeMonthlyBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "payroll_month", nullable = false, length = 7)
    private String payrollMonth;

    @Column(nullable = false)
    private BigDecimal bonusAmount;

    @CreationTimestamp
    private LocalDateTime calculationTime;
}
