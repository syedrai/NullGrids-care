package com.medicare.repository;

import com.medicare.entity.Doctor;
import com.medicare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);
    Optional<Doctor> findByUser(User user);
    List<Doctor> findBySpecializationContainingIgnoreCase(String keyword);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.user.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<Doctor> searchByNameOrSpecialization(@Param("kw") String keyword);
}
