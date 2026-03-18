package com.medicare.repository;

import com.medicare.entity.Appointment;
import com.medicare.entity.Doctor;
import com.medicare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    // Check if a slot is already booked
    boolean existsByDoctorAndAppointmentDateAndTimeSlotAndStatusNot(
        Doctor doctor, LocalDate date, String timeSlot, Appointment.AppointmentStatus status);

    // Get booked slots for a doctor on a date
    @Query("SELECT a.timeSlot FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDate = :date AND a.status != 'CANCELLED' AND a.status != 'REJECTED'")
    List<String> findBookedSlots(@Param("doctor") Doctor doctor, @Param("date") LocalDate date);

    // Appointments for a doctor on a specific date
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);

    // Appointments on a date (for leave cancellation)
    List<Appointment> findByDoctorAndAppointmentDateAndStatusNot(
        Doctor doctor, LocalDate date, Appointment.AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'PENDING'")
    long countPending();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'CONFIRMED'")
    long countConfirmed();

    // For chart: appointments per day (last 7 days)
    @Query(value = "SELECT DATE(appointment_date), COUNT(*) FROM appointments WHERE appointment_date >= :since GROUP BY DATE(appointment_date) ORDER BY DATE(appointment_date)", nativeQuery = true)
    List<Object[]> countByDateSince(@Param("since") LocalDate since);

    // For chart: per doctor
    @Query("SELECT a.doctor.user.name, COUNT(a) FROM Appointment a GROUP BY a.doctor")
    List<Object[]> countByDoctor();

    // Status breakdown
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countByStatus();
}
