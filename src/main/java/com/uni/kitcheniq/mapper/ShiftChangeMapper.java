package com.uni.kitcheniq.mapper;

import com.uni.kitcheniq.dto.ShiftChangeDTO;
import com.uni.kitcheniq.models.ShiftChange;
import org.springframework.stereotype.Component;

@Component
public class ShiftChangeMapper {

    public ShiftChangeDTO toShiftChangeDTO(ShiftChange shiftChange) {
        return ShiftChangeDTO.builder()
                .id(shiftChange.getId())
                .outgoingEmployeeId(shiftChange.getOutgoingEmployee().getId())
                .outgoingEmployeeName(shiftChange.getOutgoingEmployee().getName() + " " + 
                    (shiftChange.getOutgoingEmployee().getLastName() != null ? shiftChange.getOutgoingEmployee().getLastName() : ""))
                .incomingEmployeeId(shiftChange.getIncomingEmployee().getId())
                .incomingEmployeeName(shiftChange.getIncomingEmployee().getName() + " " + 
                    (shiftChange.getIncomingEmployee().getLastName() != null ? shiftChange.getIncomingEmployee().getLastName() : ""))
                .changeDateTime(shiftChange.getChangeDateTime())
                .notes(shiftChange.getNotes())
                .build();
    }
}
