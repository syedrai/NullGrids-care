package com.medicare.controller;

import com.medicare.entity.*;
import com.medicare.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private SecurityService securityService;
    @Autowired private DoctorLeaveService leaveService;
    @Autowired private NotificationService notificationService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        ObjectMapper mapper = new ObjectMapper();
        // Stats
        model.addAttribute("totalUsers",         safeGet(userService::countAll, 0L));
        model.addAttribute("totalDoctors",        safeGet(doctorService::countAll, 0L));
        model.addAttribute("totalAppointments",   safeGet(appointmentService::countAll, 0L));
        model.addAttribute("pendingAppointments", safeGet(appointmentService::countPending, 0L));
        model.addAttribute("criticalAlerts",      safeGet(securityService::getCriticalCount, 0L));
        model.addAttribute("failedLogins",        safeGet(securityService::getFailedLoginCount, 0L));
        model.addAttribute("pendingLeaves",       safeGet(() -> leaveService.getUpcoming().stream()
            .filter(l -> l.getStatus() == DoctorLeave.LeaveStatus.PENDING).count(), 0L));

        // Chart data as JSON strings
        model.addAttribute("apptPerDayJson",  safeJson(mapper, () -> appointmentService.getAppointmentsPerDay(7)));
        model.addAttribute("doctorUtilJson",  safeJson(mapper, appointmentService::getDoctorUtilization));
        model.addAttribute("statusJson",      safeJson(mapper, appointmentService::getStatusBreakdown));

        var logs = safeGet(securityService::getRecentLogs, List.of());
        model.addAttribute("recentLogs", logs.subList(0, Math.min(5, logs.size())));

        // Patient registrations per day (last 7 days)
        model.addAttribute("patientRegJson", safeJson(mapper, () -> {
            Map<String,Long> regMap = new LinkedHashMap<>();
            userService.getPatientRegistrationsByDate(LocalDateTime.now().minusDays(7))
                .forEach(r -> regMap.put(r[0].toString(), (Long) r[1]));
            return regMap;
        }));

        // Leave vs cancelled
        model.addAttribute("leaveImpactJson", safeJson(mapper, () -> {
            Map<String,Long> leaveMap = new LinkedHashMap<>();
            leaveMap.put("Approved Leaves", leaveService.countApproved());
            leaveMap.put("Cancellations from Leave",
                appointmentService.getAll().stream()
                    .filter(a -> a.getRescheduledNote() != null && !a.getRescheduledNote().isBlank())
                    .count());
            return leaveMap;
        }));

        return "admin/dashboard";
    }

    private <T> T safeGet(java.util.concurrent.Callable<T> fn, T fallback) {
        try { return fn.call(); } catch (Exception e) { return fallback; }
    }

    private String safeJson(ObjectMapper mapper, java.util.concurrent.Callable<?> fn) {
        try { return mapper.writeValueAsString(fn.call()); } catch (Exception e) { return "{}"; }
    }

    @GetMapping("/users")
    public String users(@RequestParam(required=false) String search, Model model) {
        var all = userService.getAll();
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            all = all.stream().filter(u ->
                u.getName().toLowerCase().contains(q) ||
                u.getEmail().toLowerCase().contains(q)).toList();
        }
        model.addAttribute("users", all);
        model.addAttribute("search", search);
        return "admin/users";
    }

    @GetMapping("/users/profile/{id}")
    public String patientProfile(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        // IDOR fix: only allow viewing PATIENT profiles
        if (user == null || user.getRole() != User.Role.PATIENT) {
            return "redirect:/admin/users?error=notfound";
        }
        var appointments = appointmentService.getByPatient(user);
        long completedCount = appointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED).count();
        long pendingCount = appointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING).count();
        model.addAttribute("patient", user);
        model.addAttribute("appointments", appointments);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        return "admin/patient-profile";
    }

    @GetMapping("/users/toggle/{id}")
    public String toggleLock(@PathVariable Long id) {
        userService.toggleLock(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/doctors")
    public String doctors(@RequestParam(required=false) String search, Model model) {
        var all = search != null && !search.isBlank()
            ? doctorService.search(search)
            : doctorService.getAll();
        model.addAttribute("doctors", all);
        model.addAttribute("search", search);
        model.addAttribute("pendingDoctors", userService.getAll().stream()
            .filter(u -> u.getRole() == User.Role.DOCTOR && doctorService.getByUser(u).isEmpty())
            .toList());
        return "admin/doctors";
    }

    // Complete doctor profile after they register
    @PostMapping("/doctors/complete")
    public String completeDoctorProfile(@RequestParam Long userId,
                                        @RequestParam String specialization,
                                        @RequestParam int experienceYears,
                                        @RequestParam String availableDays,
                                        @RequestParam String availableTime,
                                        @RequestParam String phone) {
        User user = userService.getById(userId);
        if (user != null && user.getRole() == User.Role.DOCTOR) {
            Doctor doc = new Doctor();
            doc.setUser(user);
            doc.setSpecialization(specialization);
            doc.setExperienceYears(experienceYears);
            doc.setAvailableDays(availableDays);
            doc.setAvailableTime(availableTime);
            doc.setPhone(phone);
            doctorService.save(doc);
            notificationService.send(user, "✅ Your doctor profile is now active! You can login and manage appointments.", "PROFILE_ACTIVATED");
        }
        return "redirect:/admin/doctors?completed=true";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(@RequestParam String name, @RequestParam String email,
                            @RequestParam String specialization, @RequestParam int experienceYears,
                            @RequestParam String availableDays, @RequestParam String availableTime,
                            @RequestParam String phone, @RequestParam String medicalCouncilId) {
        User u = new User();
        u.setName(name); u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Doctor@123"));
        u.setRole(User.Role.DOCTOR);
        u.setMedicalCouncilId(medicalCouncilId.toUpperCase());
        userService.save(u);
        Doctor d = new Doctor();
        d.setUser(u); d.setSpecialization(specialization);
        d.setExperienceYears(experienceYears); d.setAvailableDays(availableDays);
        d.setAvailableTime(availableTime); d.setPhone(phone);
        doctorService.save(d);
        return "redirect:/admin/doctors?added=true";
    }

    @PostMapping("/doctors/fee/{id}")
    public String updateFee(@PathVariable Long id,
                            @RequestParam double consultationFee,
                            @RequestParam String feeType) {
        Doctor d = doctorService.getById(id);
        if (d != null) {
            d.setConsultationFee(consultationFee);
            d.setFeeType(feeType);
            doctorService.save(d);
        }
        return "redirect:/admin/doctors?feeUpdated=true";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        ObjectMapper mapper = new ObjectMapper();
        model.addAttribute("totalUsers",        safeGet(userService::countAll, 0L));
        model.addAttribute("totalDoctors",       safeGet(doctorService::countAll, 0L));
        model.addAttribute("totalAppointments",  safeGet(appointmentService::countAll, 0L));
        model.addAttribute("pendingAppointments",safeGet(appointmentService::countPending, 0L));
        model.addAttribute("apptPerDayJson",     safeJson(mapper, () -> appointmentService.getAppointmentsPerDay(30)));
        model.addAttribute("doctorUtilJson",     safeJson(mapper, appointmentService::getDoctorUtilization));
        model.addAttribute("statusJson",         safeJson(mapper, appointmentService::getStatusBreakdown));
        model.addAttribute("patientRegJson",     safeJson(mapper, () -> {
            Map<String,Long> m = new LinkedHashMap<>();
            userService.getPatientRegistrationsByDate(LocalDateTime.now().minusDays(30))
                .forEach(r -> m.put(r[0].toString(), (Long) r[1]));
            return m;
        }));
        model.addAttribute("leaveImpactJson",    safeJson(mapper, () -> {
            Map<String,Long> m = new LinkedHashMap<>();
            m.put("Approved Leaves", leaveService.countApproved());
            m.put("Cancellations from Leave",
                appointmentService.getAll().stream()
                    .filter(a -> a.getRescheduledNote() != null && !a.getRescheduledNote().isBlank()).count());
            return m;
        }));
        return "admin/analytics";
    }

    @GetMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.delete(id);
        return "redirect:/admin/doctors";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAll());
        return "admin/appointments";
    }

    @GetMapping("/security-logs")
    public String securityLogs(Model model) {
        model.addAttribute("logs",          securityService.getRecentLogs());
        model.addAttribute("criticalCount", securityService.getCriticalCount());
        model.addAttribute("failedLogins",  securityService.getFailedLoginCount());
        return "admin/security-logs";
    }

    @GetMapping("/leaves")
    public String leaves(Model model) {
        model.addAttribute("leaves", leaveService.getUpcoming());
        return "admin/leaves";
    }

    @GetMapping("/leaves/approve/{id}")
    public String approveLeave(@PathVariable Long id) {
        leaveService.approveLeave(id);
        return "redirect:/admin/leaves?approved=true";
    }

    @GetMapping("/leaves/reject/{id}")
    public String rejectLeave(@PathVariable Long id) {
        leaveService.rejectLeave(id);
        return "redirect:/admin/leaves";
    }
}
