package com.example.vibe_store.entity.grade;

import com.example.vibe_store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "grade_assignments")
public class GradeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY)
    private Store store;

    @CreationTimestamp
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive;
}
