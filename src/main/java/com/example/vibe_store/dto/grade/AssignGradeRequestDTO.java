package com.example.vibe_store.dto.grade;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AssignGradeRequestDTO(
        @NotNull(message = "Grade ID must be provided")
        Integer gradeId, // for old grade, set date in service and mark as inactive
                         // if there is an active one, throw an error

        Integer storeId,

        List<Integer> employeeIds,

        @NotNull(message = "Start date must be provided")
        LocalDateTime startDate,

        @NotNull(message = "End date must be provided")
        LocalDateTime endDate
) {}