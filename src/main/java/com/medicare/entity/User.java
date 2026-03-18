package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    // Patient fields
    @Column private String phone;
    @Column private String bloodGroup;
    @Column private Integer age;
    @Column private String gender;
    @Column private String address;

    // Doctor verification — Medical Council Registration Number
    @Column(unique = true)
    private String medicalCouncilId;

    // Security
    @Column(nullable = false) private boolean enabled = true;
    @Column(nullable = false) private boolean accountLocked = false;
    @Column private LocalDateTime lockTime;
    @Column(nullable = false) private int failedLoginAttempts = 0;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { PATIENT, DOCTOR, ADMIN }
}
