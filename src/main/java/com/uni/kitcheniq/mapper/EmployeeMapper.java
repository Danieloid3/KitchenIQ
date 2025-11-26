package com.uni.kitcheniq.mapper;

import com.uni.kitcheniq.dto.CreateEmployeeDTO;
import com.uni.kitcheniq.dto.EmployeeDTO;
import com.uni.kitcheniq.enums.EmployeeType;
import com.uni.kitcheniq.models.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class EmployeeMapper {

    public EmployeeDTO toEmployeeDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .lastName(employee.getLastName())
                .idNumber(employee.getIdNumber())
                .position(employee.getPosition())
                .hourlyRate(employee.getHourlyRate())
                .contractDate(employee.getContractDate())
                .employeeType(employee.getType())
                .build();
    }

    public Employee toEmployee(CreateEmployeeDTO dto) {
        return Employee.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .lastName(dto.getLastName())
                .idNumber(dto.getIdNumber())
                .position(dto.getPosition())
                .hourlyRate(dto.getHourlyRate())
                .contractDate(LocalDate.now())
                .password("")
                .type(EmployeeType.WAITER)
                .build();
    }
}
