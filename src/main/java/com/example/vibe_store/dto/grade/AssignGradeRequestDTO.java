package com.example.vibe_store.dto.grade;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AssignGradeRequestDTO {

    @NotNull(message = "Grade ID must be provided")
    private Integer gradeId; // for old grade, set date in service and mark as inactive
                             // if there is an active one, throw an error

    private Integer storeId;

    private List<Integer> employeeIds;

    @NotNull(message = "Start date must be provided")
    private LocalDateTime startDate;

    @NotNull(message = "End date must be provided")
    private LocalDateTime endDate;
}
