package com.uni.kitcheniq.dto;

import com.uni.kitcheniq.enums.EmployeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEmployeeDTO {
    private String name;
    private String lastName;
    private String idNumber;
    private EmployeeType position;
    private BigDecimal hourlyRate;
    private String password;
}
