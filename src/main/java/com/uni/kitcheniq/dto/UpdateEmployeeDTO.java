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
public class UpdateEmployeeDTO {
    private String name;
    private String lastName;
    private EmployeeType position;
    private BigDecimal hourlyRate;
}
