package com.medicare.repository;

import com.medicare.entity.Doctor;
import com.medicare.entity.DoctorLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {

    List<DoctorLeave> findByDoctor(Doctor doctor);

    // Check if doctor has an approved leave covering a specific date
    @Query("SELECT COUNT(dl) > 0 FROM DoctorLeave dl WHERE dl.doctor = :doctor " +
           "AND dl.status = 'APPROVED' " +
           "AND :date BETWEEN dl.leaveStartDate AND dl.leaveEndDate")
    boolean isDoctorOnLeave(@Param("doctor") Doctor doctor, @Param("date") LocalDate date);

    // Check for overlapping leave (any status) to prevent duplicates
    @Query("SELECT COUNT(dl) > 0 FROM DoctorLeave dl WHERE dl.doctor = :doctor " +
           "AND dl.status != 'REJECTED' " +
           "AND dl.leaveStartDate <= :endDate AND dl.leaveEndDate >= :startDate")
    boolean hasOverlappingLeave(@Param("doctor") Doctor doctor,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT dl FROM DoctorLeave dl WHERE dl.leaveEndDate >= :today ORDER BY dl.leaveStartDate")
    List<DoctorLeave> findUpcoming(@Param("today") LocalDate today);

    @Query("SELECT COUNT(dl) FROM DoctorLeave dl WHERE dl.status = 'APPROVED'")
    long countApproved();
}
