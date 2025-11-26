package com.uni.kitcheniq.dto;

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
    private String position;
    private BigDecimal hourlyRate;
}
