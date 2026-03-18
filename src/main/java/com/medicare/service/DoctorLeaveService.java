package com.medicare.service;

import com.medicare.entity.Doctor;
import com.medicare.entity.DoctorLeave;
import com.medicare.repository.DoctorLeaveRepository;
import com.medicare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorLeaveService {

    @Autowired private DoctorLeaveRepository leaveRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentService appointmentService;
    @Autowired private NotificationService notificationService;

    public String applyLeave(Doctor doctor, LocalDate startDate, LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate))
            return "End date cannot be before start date.";
        if (startDate.isBefore(LocalDate.now()))
            return "Leave start date cannot be in the past.";
        if (leaveRepository.hasOverlappingLeave(doctor, startDate, endDate))
            return "You already have a leave request overlapping these dates.";

        DoctorLeave leave = new DoctorLeave();
        leave.setDoctor(doctor);
        leave.setLeaveStartDate(startDate);
        leave.setLeaveEndDate(endDate);
        leave.setReason(reason);
        leaveRepository.save(leave);
        return "success";
    }

    public boolean isDoctorOnLeave(Doctor doctor, LocalDate date) {
        return leaveRepository.isDoctorOnLeave(doctor, date);
    }

    public boolean isDoctorOnLeaveToday(Doctor doctor) {
        return isDoctorOnLeave(doctor, LocalDate.now());
    }

    public List<DoctorLeave> getByDoctor(Doctor doctor) {
        return leaveRepository.findByDoctor(doctor);
    }

    public List<DoctorLeave> getUpcoming() {
        return leaveRepository.findUpcoming(LocalDate.now());
    }

    public void approveLeave(Long leaveId) {
        leaveRepository.findById(leaveId).ifPresent(leave -> {
            leave.setStatus(DoctorLeave.LeaveStatus.APPROVED);
            leaveRepository.save(leave);

            Doctor doctor = leave.getDoctor();

            // Cancel all appointments in the leave range
            int totalCancelled = 0;
            LocalDate d = leave.getLeaveStartDate();
            while (!d.isAfter(leave.getLeaveEndDate())) {
                List<?> cancelled = appointmentService.cancelForLeave(doctor, d);
                totalCancelled += cancelled.size();
                d = d.plusDays(1);
            }

            // Notify peers
            List<Doctor> peers = doctorRepository.findBySpecialization(doctor.getSpecialization())
                .stream().filter(p -> !p.getId().equals(doctor.getId())).toList();
            for (Doctor peer : peers) {
                notificationService.send(peer.getUser(),
                    "Dr. " + doctor.getUser().getName() + " is on leave from " +
                    leave.getLeaveStartDate() + " to " + leave.getLeaveEndDate() +
                    ". " + totalCancelled + " patient(s) may rebook with you.",
                    "PEER_LEAVE_NOTICE");
            }

            // Notify doctor
            notificationService.send(doctor.getUser(),
                "Your leave from " + leave.getLeaveStartDate() + " to " + leave.getLeaveEndDate() +
                " has been approved. " + totalCancelled + " appointment(s) were cancelled.",
                "LEAVE_APPROVED");
        });
    }

    public void rejectLeave(Long leaveId) {
        leaveRepository.findById(leaveId).ifPresent(leave -> {
            leave.setStatus(DoctorLeave.LeaveStatus.REJECTED);
            leaveRepository.save(leave);
            notificationService.send(leave.getDoctor().getUser(),
                "Your leave request from " + leave.getLeaveStartDate() +
                " to " + leave.getLeaveEndDate() + " was rejected.",
                "LEAVE_REJECTED");
        });
    }

    public long countApproved() { return leaveRepository.countApproved(); }
}
