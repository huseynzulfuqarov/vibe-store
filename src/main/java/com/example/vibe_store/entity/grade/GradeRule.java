package com.example.vibe_store.entity.grade;

import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.enums.TargetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "grade_rules")
public class GradeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Position position;

    private BigDecimal minThreshold;

    private BigDecimal maxThreshold;

    private BigDecimal fixedAmount;

    private BigDecimal percentage;

    private BigDecimal sharePercentage;
}
