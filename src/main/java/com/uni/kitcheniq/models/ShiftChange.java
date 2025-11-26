package com.uni.kitcheniq.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift_change")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outgoing_employee_id", nullable = false)
    private Employee outgoingEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incoming_employee_id", nullable = false)
    private Employee incomingEmployee;

    @Column(name = "change_date_time", nullable = false)
    private LocalDateTime changeDateTime;

    @Column(name = "notes")
    private String notes;
}
