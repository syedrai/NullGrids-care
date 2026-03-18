package com.medicare.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private String type; // APPOINTMENT_CANCELLED, LEAVE_APPROVED, RESCHEDULED, etc.

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
