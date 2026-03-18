package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime attemptTime = LocalDateTime.now();

    @Column(nullable = false)
    private boolean success;
}
