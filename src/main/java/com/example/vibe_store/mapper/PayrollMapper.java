package com.example.vibe_store.mapper;

import com.example.vibe_store.dto.payroll.PayrollResponseDTO;
import com.example.vibe_store.entity.employee.Payroll;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    @Mapping(source = "id", target = "payrollId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(expression = "java(payroll.getEmployee().getFirstName() + \" \" + payroll.getEmployee().getLastName())", target = "employeeName")
    @Mapping(source = "store.storeName", target = "storeName")
    PayrollResponseDTO toResponse(Payroll payroll);
}