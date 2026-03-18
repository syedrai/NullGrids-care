package com.medicare.repository;

import com.medicare.entity.SecurityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long> {
    List<SecurityLog> findTop50ByOrderByTimestampDesc();
    List<SecurityLog> findBySeverity(SecurityLog.Severity severity);

    @Query("SELECT COUNT(s) FROM SecurityLog s WHERE s.severity = 'CRITICAL'")
    long countCritical();

    @Query("SELECT COUNT(s) FROM SecurityLog s WHERE s.eventType = 'LOGIN_FAIL'")
    long countFailedLogins();
}
