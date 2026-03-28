package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePositionRequestDTO {

    @NotBlank(message = "Position name cannot be blank")
    private String positionName;
}
