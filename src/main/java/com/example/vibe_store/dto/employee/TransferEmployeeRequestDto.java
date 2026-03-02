package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferEmployeeRequestDto {

    @NotNull(message = "Isci id-si bos ola bilmez")
    private Integer employeeId;

    @NotNull(message = "Target store bos ola bilmez")
    private Integer targetStoreId;

    @NotNull(message = "Target position bos ola bilmez")
    private Integer targetPositionId;

    @Min(value = 370, message = "Maaş az ola bilməz")
    @NotNull(message = "Maas bos ola bilmez")
    private BigDecimal newSalary;
}
