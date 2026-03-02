package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateEmployeeRequestDto {

    @Size(min = 4, max = 20, message = "Ad uzunlugu minimum 4, max 20 ola biler")
    private String firstName;

    @Size(min = 4, max = 20, message = "Soyad uzunlugu minimum 4, max 20 ola biler")
    private String lastName;

    @Min(value = 18, message = "Yas 18 den kiccik ola bilmez")
    @Max(value = 65, message = "Yas 65 den cox ola bilmez")
    private Byte age;

    @NotBlank(message = "bos ola bilmez")
    @Email(message = "email formati sehvdir")
    private String email;
}
