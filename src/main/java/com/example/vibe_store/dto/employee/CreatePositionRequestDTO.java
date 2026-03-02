package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreatePositionRequestDTO {

    @NotBlank(message = "bos ola bilmez")
    private String positionName;
}
