package com.medicare.controller;

import com.medicare.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired private AuthService authService;

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String error,
                            @RequestParam(required=false) String logout, Model model) {
        if (error  != null) model.addAttribute("error",   "Invalid credentials or account locked.");
        if (logout != null) model.addAttribute("success", "Logged out successfully.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(defaultValue="patient") String type, Model model) {
        model.addAttribute("type", type);
        return "register";
    }

    @PostMapping("/register/patient")
    public String registerPatient(@RequestParam String name, @RequestParam String email,
                                  @RequestParam String password, @RequestParam String phone,
                                  @RequestParam(required=false) String bloodGroup,
                                  @RequestParam(required=false) Integer age,
                                  @RequestParam(required=false) String gender,
                                  @RequestParam(required=false) String address,
                                  Model model) {
        String result = authService.registerPatient(name, email, password, phone, bloodGroup, age, gender, address);
        if (result.equals("success")) {
            model.addAttribute("success", "Patient account created! Please login.");
            return "login";
        }
        model.addAttribute("error", result);
        model.addAttribute("type", "patient");
        return "register";
    }

    @PostMapping("/register/doctor")
    public String registerDoctor(@RequestParam String name, @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String medicalCouncilId,
                                 @RequestParam String phone, Model model) {
        String result = authService.registerDoctor(name, email, password, medicalCouncilId, phone);
        if (result.equals("success")) {
            model.addAttribute("success", "Doctor account created! Admin will complete your profile. Please login.");
            return "login";
        }
        model.addAttribute("error", result);
        model.addAttribute("type", "doctor");
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN"  -> "redirect:/admin/dashboard";
            case "ROLE_DOCTOR" -> "redirect:/doctor/dashboard";
            default            -> "redirect:/patient/dashboard";
        };
    }
}
