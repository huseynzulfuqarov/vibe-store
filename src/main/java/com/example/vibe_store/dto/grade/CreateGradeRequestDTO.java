package com.example.vibe_store.dto.grade;

import com.example.vibe_store.enums.GradeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateGradeRequestDTO {

    @NotBlank(message = "Grade name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String gradeName;

    @NotNull(message = "Grade type must be selected")
    private GradeType gradeType;

    @Valid
    @NotNull(message = "At least one rule must be provided for the grade")
    private List<CreateGradeRuleRequestDTO> rules;
}