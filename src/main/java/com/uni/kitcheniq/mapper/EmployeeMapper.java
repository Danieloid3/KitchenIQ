package com.uni.kitcheniq.mapper;

import com.uni.kitcheniq.dto.CreateEmployeeDTO;
import com.uni.kitcheniq.dto.EmployeeDTO;
import com.uni.kitcheniq.models.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmployeeMapper {

    public EmployeeDTO toEmployeeDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .lastName(employee.getLastName())
                .employeeType(employee.getType())
                .hourlyRate(employee.getHourlyRate())
                .contractDate(employee.getContractDate())
                .build();
    }

    public Employee toEmployee(CreateEmployeeDTO dto, String encodedPassword) {
        return Employee.builder()
                .id(dto.getIdNumber())
                .name(dto.getName())
                .lastName(dto.getLastName())
                .type(dto.getPosition())
                .hourlyRate(dto.getHourlyRate())
                .contractDate(LocalDate.now())
                .password(encodedPassword)
                .build();
    }
}
