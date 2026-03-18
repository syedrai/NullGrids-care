package com.medicare.controller;

import com.medicare.entity.*;
import com.medicare.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private DoctorLeaveService leaveService;
    @Autowired private NotificationService notificationService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Doctor currentDoctor(Authentication auth) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        return user != null ? doctorService.getByUser(user).orElse(null) : null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        LocalDate today = LocalDate.now();
        var allAppts = appointmentService.getByDoctor(doctor);
        var todayAppts = appointmentService.getByDoctorAndDate(doctor, today);
        boolean onLeaveToday = leaveService.isDoctorOnLeaveToday(doctor);
        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", allAppts);
        model.addAttribute("todayAppointments", todayAppts);
        model.addAttribute("pendingCount", allAppts.stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING).count());
        model.addAttribute("confirmedCount", allAppts.stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONFIRMED).count());
        model.addAttribute("todayCount", todayAppts.size());
        model.addAttribute("leaves", leaveService.getByDoctor(doctor));
        model.addAttribute("onLeaveToday", onLeaveToday);
        model.addAttribute("unreadCount", notificationService.countUnread(doctor.getUser()));
        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(@RequestParam(required=false) String date,
                               @RequestParam(required=false) String status,
                               Authentication auth, Model model) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        LocalDate filterDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        var appts = filterDate != null
            ? appointmentService.getByDoctorAndDate(doctor, filterDate)
            : appointmentService.getByDoctor(doctor);
        if (status != null && !status.isBlank()) {
            try {
                Appointment.AppointmentStatus s = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
                appts = appts.stream().filter(a -> a.getStatus() == s).toList();
            } catch (Exception ignored) {}
        }
        model.addAttribute("appointments", appts);
        model.addAttribute("filterDate", filterDate);
        model.addAttribute("filterStatus", status);
        return "doctor/appointments";
    }

    @GetMapping("/appointment/confirm/{id}")
    public String confirm(@PathVariable Long id, Authentication auth) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        Appointment a = appointmentService.getById(id);
        if (a != null && a.getDoctor().getId().equals(doctor.getId()))
            appointmentService.updateStatus(id, Appointment.AppointmentStatus.CONFIRMED);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/appointment/reject/{id}")
    public String reject(@PathVariable Long id, Authentication auth) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        Appointment a = appointmentService.getById(id);
        if (a != null && a.getDoctor().getId().equals(doctor.getId()))
            appointmentService.updateStatus(id, Appointment.AppointmentStatus.REJECTED);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointment/notes/{id}")
    public String addNotes(@PathVariable Long id, @RequestParam String notes, Authentication auth) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        Appointment a = appointmentService.getById(id);
        if (a != null && a.getDoctor().getId().equals(doctor.getId())) {
            appointmentService.updateNotes(id, notes);
            appointmentService.updateStatus(id, Appointment.AppointmentStatus.COMPLETED);
        }
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String availableDays,
                                @RequestParam String availableTime,
                                @RequestParam(required=false) String bio, Authentication auth) {
        Doctor doctor = currentDoctor(auth);
        if (doctor != null) {
            doctor.setAvailableDays(availableDays);
            doctor.setAvailableTime(availableTime);
            doctor.setBio(bio);
            doctorService.save(doctor);
        }
        return "redirect:/doctor/profile?updated=true";
    }

    // Apply for leave (date range)
    @PostMapping("/leave/apply")
    public String applyLeave(@RequestParam String leaveStartDate,
                             @RequestParam String leaveEndDate,
                             @RequestParam(required=false) String reason,
                             Authentication auth, Model model) {
        Doctor doctor = currentDoctor(auth);
        if (doctor == null) return "redirect:/login?error=profile";
        String result = leaveService.applyLeave(doctor,
            LocalDate.parse(leaveStartDate), LocalDate.parse(leaveEndDate),
            reason != null ? reason : "");
        if (!result.equals("success")) {
            model.addAttribute("leaveError", result);
            return "redirect:/doctor/dashboard?leaveError=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/doctor/dashboard?leaveApplied=true";
    }

    @GetMapping("/notifications")
    public String notifications(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        notificationService.markAllRead(user);
        model.addAttribute("notifications", notificationService.getAll(user));
        return "doctor/notifications";
    }

    // Add special patient by doctor
    @GetMapping("/add-patient")
    public String addPatientPage() { return "doctor/add-patient"; }

    @PostMapping("/add-patient")
    public String addPatient(@RequestParam String name, @RequestParam String email,
                             @RequestParam String phone, @RequestParam(required=false) String age,
                             @RequestParam(required=false) String gender,
                             @RequestParam(required=false) String bloodGroup,
                             @RequestParam(required=false) String address,
                             @RequestParam(required=false) String medicalHistory,
                             Authentication auth, Model model) {
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "A user with this email already exists.");
            return "doctor/add-patient";
        }
        User u = new User();
        u.setName(name); u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Patient@123"));
        u.setRole(User.Role.PATIENT);
        u.setPhone(phone);
        if (age != null && !age.isBlank()) u.setAge(Integer.parseInt(age));
        u.setGender(gender); u.setBloodGroup(bloodGroup); u.setAddress(address);
        userService.save(u);
        return "redirect:/doctor/add-patient?added=true";
    }
}