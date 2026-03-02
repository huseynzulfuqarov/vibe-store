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

    @NotBlank(message = "Grade adı boş ola bilməz")
    @Size(min = 2, max = 50, message = "Ad 2-50 simvol arasında olmalıdır")
    private String gradeName;

    @NotNull(message = "Grade tipi seçilməlidir")
    private GradeType gradeType;

    @Valid
    @NotNull(message = "Grade üçün ən azı bir qayda (Rule) göndərilməlidir")
    private List<GradeRuleDto> rules;
}
