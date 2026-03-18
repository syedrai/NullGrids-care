package com.medicare.service;

import com.medicare.entity.Appointment;
import com.medicare.entity.Doctor;
import com.medicare.entity.User;
import com.medicare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private NotificationService notificationService;
    @Autowired @org.springframework.context.annotation.Lazy private DoctorLeaveService leaveService;

    private static final List<String> ALL_SLOTS = List.of(
        "09:00 AM","09:30 AM","10:00 AM","10:30 AM","11:00 AM","11:30 AM",
        "02:00 PM","02:30 PM","03:00 PM","03:30 PM","04:00 PM"
    );

    public void book(User patient, Doctor doctor, LocalDate date, String timeSlot, String reason) {
        if (date.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Cannot book appointments on past dates.");
        // Real-time: if booking today, slot must be at least 1 hour from now
        if (date.equals(LocalDate.now())) {
            LocalTime slotTime = parseSlot(timeSlot);
            if (slotTime != null && slotTime.isBefore(LocalTime.now().plusHours(1)))
                throw new IllegalArgumentException("Slots must be at least 1 hour from now. Please choose a later slot.");
        }
        if (leaveService.isDoctorOnLeave(doctor, date))
            throw new IllegalArgumentException("Dr. " + doctor.getUser().getName() + " is on approved leave on " + date + ".");
        if (isSlotTaken(doctor, date, timeSlot))
            throw new IllegalArgumentException("This slot is already booked.");

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setAppointmentDate(date);
        appt.setTimeSlot(timeSlot);
        appt.setReason(reason);
        appt.setStatus(Appointment.AppointmentStatus.PENDING);
        appointmentRepository.save(appt);
        notificationService.send(patient,
            "Appointment booked with Dr. " + doctor.getUser().getName() + " on " + date + " at " + timeSlot,
            "APPOINTMENT_BOOKED");
    }

    private LocalTime parseSlot(String slot) {
        try {
            return LocalTime.parse(slot.trim().toUpperCase(),
                DateTimeFormatter.ofPattern("hh:mm a"));
        } catch (Exception e) { return null; }
    }

    // Check if slot is taken
    public boolean isSlotTaken(Doctor doctor, LocalDate date, String timeSlot) {
        return appointmentRepository.existsByDoctorAndAppointmentDateAndTimeSlotAndStatusNot(
            doctor, date, timeSlot, Appointment.AppointmentStatus.CANCELLED);
    }

    // Get available slots for a doctor on a date
    public List<String> getAvailableSlots(Doctor doctor, LocalDate date) {
        List<String> booked = appointmentRepository.findBookedSlots(doctor, date);
        boolean isToday = date.equals(LocalDate.now());
        LocalTime cutoff = LocalTime.now().plusHours(1);
        return ALL_SLOTS.stream().filter(s -> {
            if (booked.contains(s)) return false;
            if (isToday) {
                LocalTime t = parseSlot(s);
                return t != null && !t.isBefore(cutoff);
            }
            return true;
        }).toList();
    }

    public List<Appointment> getByPatient(User patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getByDoctorAndDate(Doctor doctor, LocalDate date) {
        return appointmentRepository.findByDoctorAndAppointmentDate(doctor, date);
    }

    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    public Appointment getById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    public void updateStatus(Long id, Appointment.AppointmentStatus status) {
        appointmentRepository.findById(id).ifPresent(a -> {
            a.setStatus(status);
            appointmentRepository.save(a);
        });
    }

    public void updateNotes(Long id, String notes) {
        appointmentRepository.findById(id).ifPresent(a -> {
            a.setDoctorNotes(notes);
            appointmentRepository.save(a);
        });
    }

    public void cancel(Long id) {
        updateStatus(id, Appointment.AppointmentStatus.CANCELLED);
    }

    // Cancel all appointments for a doctor on a given date (doctor leave)
    public List<Appointment> cancelForLeave(Doctor doctor, LocalDate date) {
        List<Appointment> affected = appointmentRepository
            .findByDoctorAndAppointmentDateAndStatusNot(doctor, date, Appointment.AppointmentStatus.CANCELLED);
        for (Appointment a : affected) {
            a.setStatus(Appointment.AppointmentStatus.CANCELLED);
            a.setRescheduledNote("Dr. " + doctor.getUser().getName() + " is on leave. Please rebook.");
            appointmentRepository.save(a);
            notificationService.send(a.getPatient(),
                "⚠️ Your appointment with Dr. " + doctor.getUser().getName() +
                " on " + date + " has been cancelled due to doctor leave. Please rebook.",
                "APPOINTMENT_CANCELLED");
        }
        return affected;
    }

    public long countPending()   { return appointmentRepository.countPending(); }
    public long countConfirmed() { return appointmentRepository.countConfirmed(); }
    public long countAll()       { return appointmentRepository.count(); }

    // Chart data
    public Map<String, Long> getAppointmentsPerDay(int days) {
        List<Object[]> rows = appointmentRepository.countByDateSince(LocalDate.now().minusDays(days));
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(r[0].toString(), (Long) r[1]));
        return map;
    }

    public Map<String, Long> getDoctorUtilization() {
        List<Object[]> rows = appointmentRepository.countByDoctor();
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(r[0].toString(), (Long) r[1]));
        return map;
    }

    public Map<String, Long> getStatusBreakdown() {
        List<Object[]> rows = appointmentRepository.countByStatus();
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(r[0].toString(), (Long) r[1]));
        return map;
    }
}
