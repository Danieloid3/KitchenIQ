package com.uni.kitcheniq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftChangeDTO {
    private String outgoingEmployeeId;
    private String incomingEmployeeId;
}
