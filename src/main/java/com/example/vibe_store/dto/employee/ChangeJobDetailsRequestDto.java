package com.example.vibe_store.dto.employee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ChangeJobDetailsRequestDto {

    @NotNull(message = "Isci id-si bos ola bilmez")
    private Integer employeeId;

    private Integer targetStoreId;
    private Integer targetPositionId;

    @Min(value = 370, message = "Maaş az ola bilməz")
    private BigDecimal newSalary;
}
