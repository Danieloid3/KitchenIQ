package com.uni.kitcheniq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftChangeResponseDTO {
    private Long id;
    private String outgoingEmployeeId;
    private String outgoingEmployeeName;
    private String incomingEmployeeId;
    private String incomingEmployeeName;
    private LocalDateTime changeDateTime;
    private String message;
}
