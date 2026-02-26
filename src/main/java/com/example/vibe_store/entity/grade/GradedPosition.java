package com.example.vibe_store.entity.grade;

import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "graded_positions")
public class GradedPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Position  position;

    @ManyToOne(fetch = FetchType.LAZY)
    private GradeAssignment gradeAssignment;
}
