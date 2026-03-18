package com.medicare.repository;

import com.medicare.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    @Query("SELECT COUNT(l) FROM LoginAttempt l WHERE l.email = :email AND l.success = false AND l.attemptTime > :since")
    int countFailedAttempts(String email, LocalDateTime since);

    List<LoginAttempt> findByEmailOrderByAttemptTimeDesc(String email);
}
