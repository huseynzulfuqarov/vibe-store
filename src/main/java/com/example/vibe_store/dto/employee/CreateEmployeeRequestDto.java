package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateEmployeeRequestDto {

    @NotBlank(message = "Ad bos ola bilmez")
    private String firstName;

    @NotBlank(message = "Soyad bos ola bilmez")
    private String lastName;

    @Min(value = 18, message = "Yas 18 den kicik ola bilmez")
    private Byte age;

    @NotBlank(message = "Email bos ola bilmez")
    @Email(message = "Düzgün email formatı daxil edin")
    private String email;

    @NotNull(message = "Store bos ola bilmez")
    private Integer storeId;

    @NotNull(message = "Posizya bos ola bilmez")
    private Integer positionId;

    @NotNull(message = "Maas bos ola bilmez")
    @Min(value = 370, message = "minimum maas 370 azn ola biler")
    private BigDecimal salary;
}
