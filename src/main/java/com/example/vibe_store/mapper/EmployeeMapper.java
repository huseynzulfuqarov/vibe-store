package com.example.vibe_store.mapper;

import com.example.vibe_store.dto.employee.*;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.employee.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "id", target = "positionId")
    PositionResponseDTO toResponse(Position position);

    @Mapping(target = "id", ignore = true)
    Position toEntity(CreatePositionRequestDTO request);

    @Mapping(target = "id", ignore = true)
    Employee toEntity(HireEmployeeRequestDTO request);

    @Mapping(source = "id", target = "employeeId")
    EmployeeProfileResponseDTO toProfileResponse(Employee employee);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(UpdateEmployeeProfileRequestDTO request, @MappingTarget Employee employee);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.firstName", target = "firstName")
    @Mapping(source = "employee.lastName", target = "lastName")
    @Mapping(source = "activeWorkHistory.startDate", target = "hireDate")
    @Mapping(source = "activeWorkHistory.endDate", target = "terminationDate")
    @Mapping(source = "employee.age", target = "age")
    @Mapping(source = "employee.email", target = "email")
    @Mapping(source = "activeWorkHistory.salary", target = "currentSalary")
    @Mapping(source = "activeWorkHistory.store.storeName", target = "currentStoreName")
    @Mapping(source = "activeWorkHistory.position.positionName", target = "currentPositionName")
    AllEmployeeDetailsResponseDTO toAllDetailsResponse(Employee employee, EmployeeWorkHistory activeWorkHistory);
}