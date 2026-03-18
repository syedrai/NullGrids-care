package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column
    private String email;

    @Column(nullable = false)
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAIL, BRUTE_FORCE, ACCOUNT_LOCKED, etc.

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.INFO;

    public enum Severity {
        INFO, WARNING, CRITICAL
    }
}
