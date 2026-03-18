package com.medicare.controller;

import com.medicare.entity.*;
import com.medicare.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired private UserService userService;
    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private NotificationService notificationService;
    @Autowired private DoctorLeaveService leaveService;

    private User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = currentUser(auth);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("appointments", appointmentService.getByPatient(user));
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        return "patient/dashboard";
    }

    @GetMapping("/doctors")
    public String doctors(@RequestParam(required=false) String search, Model model) {
        model.addAttribute("doctors", search != null && !search.isBlank()
            ? doctorService.search(search) : doctorService.getAll());
        model.addAttribute("search", search);
        return "patient/doctors";
    }

    @GetMapping("/book")
    public String bookPage(Model model) {
        model.addAttribute("doctors", doctorService.getAll());
        return "patient/book";
    }

    // AJAX: get available doctors on a date (for leave modal alternatives)
    @GetMapping("/available-doctors")
    @ResponseBody
    public java.util.List<java.util.Map<String,Object>> availableDoctors(
            @RequestParam String date, @RequestParam Long excludeDoctorId) {
        LocalDate apptDate = LocalDate.parse(date);
        return doctorService.getAll().stream()
            .filter(d -> !d.getId().equals(excludeDoctorId)
                && !leaveService.isDoctorOnLeave(d, apptDate))
            .map(d -> {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("id", d.getId());
                m.put("name", d.getUser().getName());
                m.put("specialization", d.getSpecialization());
                m.put("availableSlots", appointmentService.getAvailableSlots(d, apptDate));
                return m;
            }).toList();
    }

    // AJAX: check slot availability — also checks leave and past date
    @GetMapping("/check-slot")
    @ResponseBody
    public java.util.Map<String, Object> checkSlot(@RequestParam Long doctorId,
                                                    @RequestParam String date,
                                                    @RequestParam String timeSlot) {
        Doctor doctor = doctorService.getById(doctorId);
        LocalDate apptDate = LocalDate.parse(date);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();

        // Past date check
        if (apptDate.isBefore(LocalDate.now())) {
            resp.put("error", "Cannot book appointments on past dates.");
            return resp;
        }
        // Leave check
        if (leaveService.isDoctorOnLeave(doctor, apptDate)) {
            resp.put("onLeave", true);
            resp.put("message", "Dr. " + doctor.getUser().getName() + " is on approved leave on " + apptDate + ". Please select another date.");
            return resp;
        }

        boolean taken = appointmentService.isSlotTaken(doctor, apptDate, timeSlot);
        resp.put("taken", taken);
        resp.put("onLeave", false);
        if (taken) {
            resp.put("availableSlots", appointmentService.getAvailableSlots(doctor, apptDate));
            resp.put("altDoctors", doctorService.getBySpecialization(doctor.getSpecialization())
                .stream().filter(d -> !d.getId().equals(doctorId)
                    && !leaveService.isDoctorOnLeave(d, apptDate))
                .map(d -> {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("id", d.getId());
                    m.put("name", d.getUser().getName());
                    m.put("slots", appointmentService.getAvailableSlots(d, apptDate));
                    return m;
                }).toList());
        }
        return resp;
    }

    @PostMapping("/book")
    public String bookAppointment(@RequestParam Long doctorId,
                                  @RequestParam String appointmentDate,
                                  @RequestParam String timeSlot,
                                  @RequestParam String reason,
                                  Authentication auth, Model model) {
        User patient = currentUser(auth);
        if (patient == null) return "redirect:/login";
        Doctor doctor = doctorService.getById(doctorId);
        if (doctor == null) {
            model.addAttribute("error", "Selected doctor not found.");
            model.addAttribute("doctors", doctorService.getAll());
            return "patient/book";
        }
        LocalDate date = LocalDate.parse(appointmentDate);
        try {
            appointmentService.book(patient, doctor, date, timeSlot, reason);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("doctors", doctorService.getAll());
            return "patient/book";
        }
        return "redirect:/patient/dashboard?booked=true";
    }

    @GetMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id, Authentication auth) {
        User user = currentUser(auth);
        if (user == null) return "redirect:/login";
        Appointment a = appointmentService.getById(id);
        if (a != null && a.getPatient().getEmail().equals(auth.getName())) {
            appointmentService.cancel(id);
        }
        return "redirect:/patient/dashboard";
    }

    @GetMapping("/notifications")
    public String notifications(Authentication auth, Model model) {
        User user = currentUser(auth);
        if (user == null) return "redirect:/login";
        notificationService.markAllRead(user);
        model.addAttribute("notifications", notificationService.getAll(user));
        return "patient/notifications";
    }
}
