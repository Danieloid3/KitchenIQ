package com.uni.kitcheniq.mapper;

import com.uni.kitcheniq.dto.ShiftChangeDTO;
import com.uni.kitcheniq.models.Employee;
import com.uni.kitcheniq.models.ShiftChange;
import org.springframework.stereotype.Component;

@Component
public class ShiftChangeMapper {

    public ShiftChangeDTO toShiftChangeDTO(ShiftChange shiftChange) {
        return ShiftChangeDTO.builder()
                .id(shiftChange.getId())
                .outgoingEmployeeId(shiftChange.getOutgoingEmployee().getId())
                .outgoingEmployeeName(getFullName(shiftChange.getOutgoingEmployee()))
                .incomingEmployeeId(shiftChange.getIncomingEmployee().getId())
                .incomingEmployeeName(getFullName(shiftChange.getIncomingEmployee()))
                .changeDateTime(shiftChange.getChangeDateTime())
                .notes(shiftChange.getNotes())
                .build();
    }

    private String getFullName(Employee employee) {
        String firstName = employee.getName() != null ? employee.getName() : "";
        String lastName = employee.getLastName() != null ? employee.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}
