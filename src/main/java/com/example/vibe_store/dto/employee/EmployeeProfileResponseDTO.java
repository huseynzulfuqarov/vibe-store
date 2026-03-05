package com.example.vibe_store.dto.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeProfileResponseDTO {
    private Integer employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private Byte age;
}
