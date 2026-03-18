package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String reason;

    @Column
    private String doctorNotes;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // If rescheduled due to doctor leave, note original doctor
    @Column
    private String rescheduledNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AppointmentStatus {
        PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED, RESCHEDULED
    }
}
