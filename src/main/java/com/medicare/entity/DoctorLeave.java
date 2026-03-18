package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate leaveStartDate;

    @Column(nullable = false)
    private LocalDate leaveEndDate;

    @Column
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    public enum LeaveStatus { PENDING, APPROVED, REJECTED }

    // Helper: does this leave cover the given date?
    public boolean coversDate(LocalDate date) {
        return status == LeaveStatus.APPROVED
            && !date.isBefore(leaveStartDate)
            && !date.isAfter(leaveEndDate);
    }
}
