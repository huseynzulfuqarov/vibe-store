package com.example.vibe_store.entity.grade;

import com.example.vibe_store.entity.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "graded_employees")
public class GradedEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    private GradeAssignment gradeAssignment;
}
