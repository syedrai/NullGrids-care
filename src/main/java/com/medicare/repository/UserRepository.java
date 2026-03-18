package com.medicare.repository;

import com.medicare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMedicalCouncilId(String medicalCouncilId);

    // Chart: patient registrations per day
    @Query(value = "SELECT DATE(created_at), COUNT(*) FROM users WHERE role = 'PATIENT' AND created_at >= :since GROUP BY DATE(created_at) ORDER BY DATE(created_at)", nativeQuery = true)
    List<Object[]> countPatientRegistrationsByDate(@Param("since") LocalDateTime since);
}
