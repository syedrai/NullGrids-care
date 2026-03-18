package com.medicare.service;

import com.medicare.entity.LoginAttempt;
import com.medicare.entity.SecurityLog;
import com.medicare.entity.User;
import com.medicare.repository.LoginAttemptRepository;
import com.medicare.repository.SecurityLogRepository;
import com.medicare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityService {

    @Autowired private SecurityLogRepository securityLogRepository;
    @Autowired private LoginAttemptRepository loginAttemptRepository;
    @Autowired private UserRepository userRepository;

    @Value("${security.max-login-attempts}")
    private int maxAttempts;

    @Value("${security.lockout-duration-minutes}")
    private int lockoutMinutes;

    // ─── Login Attempt Tracking ───────────────────────────────────────────────

    public void recordLoginAttempt(String email, String ip, boolean success) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ip);
        attempt.setSuccess(success);
        attempt.setAttemptTime(LocalDateTime.now());
        loginAttemptRepository.save(attempt);

        if (!success) {
            handleFailedLogin(email, ip);
        } else {
            handleSuccessfulLogin(email, ip);
        }
    }

    private void handleFailedLogin(String email, String ip) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lockoutMinutes);
        int failCount = loginAttemptRepository.countFailedAttempts(email, since);

        logEvent(null, email, "LOGIN_FAIL", ip, null,
                "Failed login attempt #" + failCount, SecurityLog.Severity.WARNING);

        if (failCount >= maxAttempts) {
            lockAccount(email, ip);
        }
    }

    private void handleSuccessfulLogin(String email, String ip) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setLockTime(null);
            userRepository.save(user);
        });
        logEvent(null, email, "LOGIN_SUCCESS", ip, null, "Successful login", SecurityLog.Severity.INFO);
    }

    private void lockAccount(String email, String ip) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            userRepository.save(user);
            logEvent(user.getId(), email, "ACCOUNT_LOCKED", ip, null,
                    "Account locked after " + maxAttempts + " failed attempts",
                    SecurityLog.Severity.CRITICAL);
        });
    }

    public boolean isAccountLocked(String email) {
        return userRepository.findByEmail(email).map(user -> {
            if (!user.isAccountLocked()) return false;
            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(lockoutMinutes).isBefore(LocalDateTime.now())) {
                // Auto-unlock after lockout period
                user.setAccountLocked(false);
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
                return false;
            }
            return true;
        }).orElse(false);
    }

    // ─── Security Event Logging ───────────────────────────────────────────────

    public void logEvent(Long userId, String email, String eventType,
                         String ip, String userAgent, String description,
                         SecurityLog.Severity severity) {
        SecurityLog log = new SecurityLog();
        log.setUserId(userId);
        log.setEmail(email);
        log.setEventType(eventType);
        log.setIpAddress(ip);
        log.setUserAgent(userAgent);
        log.setDescription(description);
        log.setSeverity(severity);
        log.setTimestamp(LocalDateTime.now());
        securityLogRepository.save(log);
    }

    public List<SecurityLog> getRecentLogs() {
        return securityLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public long getCriticalCount() {
        return securityLogRepository.countCritical();
    }

    public long getFailedLoginCount() {
        return securityLogRepository.countFailedLogins();
    }
}
