# 🏥 MediCare Secure
### Production-Grade Secure Hospital Appointment System
**Stack:** Spring Boot 3 · Spring Security · JWT · BCrypt · Thymeleaf · MySQL · Bootstrap 5

---

## ✅ Prerequisites
- Java JDK 17+
- Maven 3.6+
- MySQL 8.0+ (running on port 3306)
- VS Code with "Extension Pack for Java"

---

## 🚀 Quick Start (3 Steps)

### Step 1 — Configure Database
Open `src/main/resources/application.properties` and update:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 2 — Run the Application
Open VS Code terminal and run:
```bash
mvn spring-boot:run
```
On first startup, the app **automatically seeds** admin + 6 doctors. Watch the console for:
```
✅ Admin seeded: admin@medicare.com / Admin@123
✅ Doctor seeded: rajesh@medicare.com / Doctor@123
```

### Step 3 — Open Browser
Go to: **http://localhost:8080**

---

## 🔐 Login Credentials

| Role    | Email                   | Password    |
|---------|-------------------------|-------------|
| Admin   | admin@medicare.com      | Admin@123   |
| Doctor  | rajesh@medicare.com     | Doctor@123  |
| Doctor  | priya@medicare.com      | Doctor@123  |
| Doctor  | arun@medicare.com       | Doctor@123  |
| Patient | Register via /register  | Your choice |

---

## 🗺️ Application Flow

```
/login
  ├── ADMIN  → /admin/dashboard
  │     ├── /admin/appointments   (view all)
  │     ├── /admin/doctors        (add/delete)
  │     ├── /admin/users          (lock/unlock)
  │     └── /admin/security-logs  (audit trail)
  │
  ├── DOCTOR → /doctor/dashboard
  │     ├── /doctor/appointments  (confirm/reject/complete)
  │     └── /doctor/profile       (update availability)
  │
  └── PATIENT → /patient/dashboard
        ├── /patient/book         (book appointment)
        └── /patient/doctors      (search doctors)
```

---

## 🛡️ Security Features Implemented

### Authentication & Authorization
| Feature | Implementation |
|---------|----------------|
| Password Hashing | BCrypt with strength 12 |
| Role-Based Access | Spring Security + `@PreAuthorize` |
| Session Management | Single session per user, auto-expire |
| CSRF Protection | Spring Security CSRF tokens (auto in Thymeleaf) |

### Attack Prevention
| Attack | Protection |
|--------|------------|
| Brute Force | Account locked after 5 failed attempts (15 min) |
| SQL Injection | JPA/Hibernate parameterized queries |
| XSS | Input sanitization in AuthService + CSP headers |
| Clickjacking | `X-Frame-Options: DENY` header |
| Session Hijacking | Session invalidation on logout + cookie deletion |

### Monitoring & Audit
| Feature | Details |
|---------|---------|
| Security Event Logging | Every login attempt logged to `security_logs` table |
| Brute Force Detection | Failed attempts tracked per email with timestamps |
| Account Locking | Auto-lock + auto-unlock after cooldown period |
| Admin Alerts | Critical events visible on admin dashboard |

---

## 🗂️ Project Structure
```
src/main/java/com/medicare/
├── MediCareSecureApplication.java
├── config/
│   ├── SecurityConfig.java      ← Spring Security + RBAC + headers
│   └── DataInitializer.java     ← Auto-seed admin & doctors
├── controller/
│   ├── AuthController.java
│   ├── PatientController.java
│   ├── DoctorController.java
│   └── AdminController.java
├── entity/
│   ├── User.java                ← with accountLocked, failedAttempts
│   ├── Doctor.java
│   ├── Appointment.java
│   ├── SecurityLog.java         ← audit trail
│   └── LoginAttempt.java        ← brute force tracking
├── repository/         (5 JPA repositories)
├── service/
│   ├── AuthService.java         ← registration + XSS sanitization
│   ├── SecurityService.java     ← logging + brute force + locking
│   ├── AppointmentService.java
│   ├── DoctorService.java
│   ├── UserService.java
│   └── CustomUserDetailsService.java
└── util/
    └── JwtUtil.java             ← JWT generation & validation
```

---

## 🎓 Viva Talking Points

**Q: How is authentication handled?**
> "Spring Security handles authentication via a custom `UserDetailsService`. Passwords are hashed using BCrypt with cost factor 12. Role-based access control uses `@PreAuthorize` annotations and URL-pattern matchers ensuring patients can't access doctor or admin routes."

**Q: How did you prevent brute force attacks?**
> "The `SecurityService` tracks failed login attempts in a `login_attempts` table. After 5 failures within 15 minutes, the account is locked automatically. All events are logged to `security_logs` with severity levels — INFO, WARNING, and CRITICAL."

**Q: How is SQL injection prevented?**
> "All database operations use JPA/Hibernate with parameterized queries, which completely eliminates SQL injection. Raw SQL is never concatenated."

**Q: How is XSS prevented?**
> "User inputs are sanitized in `AuthService` before persistence. Spring Security adds `Content-Security-Policy` headers restricting script sources, and `X-Frame-Options: DENY` prevents clickjacking."

**Q: What is CSRF and how is it handled?**
> "CSRF (Cross-Site Request Forgery) tricks authenticated users into submitting malicious requests. Spring Security generates a unique CSRF token per session, and Thymeleaf automatically includes it in every form via `th:action`."

---

## ⚠️ Common Errors & Fixes

| Error | Fix |
|-------|-----|
| Access denied for root | Update password in application.properties |
| Port 8080 in use | Add `server.port=8090` to application.properties |
| Tables not created | Ensure `spring.jpa.hibernate.ddl-auto=update` |
| Dependency errors | Run `mvn clean install` in terminal |
| White label error page | Check console for missing bean or mapping errors |
