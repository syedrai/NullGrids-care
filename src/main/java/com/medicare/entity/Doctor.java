package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String specialization;

    @Column
    private int experienceYears;

    @Column
    private String availableDays;   // e.g. "Mon,Wed,Fri"

    @Column
    private String availableTime;

    @Column
    private String phone;

    @Column
    private String bio;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private double rating = 0.0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int totalRatings = 0;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private double consultationFee = 0.0;

    @Column(length = 20)
    private String feeType = "Per Checkup"; // "Per Checkup" or "Per Hour"

    // On leave today flag
    @Column(nullable = false)
    private boolean onLeave = false;
}
