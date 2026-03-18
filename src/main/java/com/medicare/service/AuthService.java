package com.medicare.service;

import com.medicare.entity.User;
import com.medicare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SecurityService securityService;

    // Register a PATIENT
    public String registerPatient(String name, String email, String password,
                                  String phone, String bloodGroup, Integer age,
                                  String gender, String address) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "Invalid email format";
        if (password == null || password.length() < 8)  return "Password must be at least 8 characters";
        if (userRepository.existsByEmail(email))         return "Email already registered";

        User user = new User();
        user.setName(sanitize(name));
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.PATIENT);
        user.setPhone(phone);
        user.setBloodGroup(bloodGroup);
        user.setAge(age);
        user.setGender(gender);
        user.setAddress(sanitize(address));
        userRepository.save(user);
        return "success";
    }

    // Register a DOCTOR — requires valid Medical Council ID
    public String registerDoctor(String name, String email, String password,
                                 String medicalCouncilId, String phone) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "Invalid email format";
        if (password == null || password.length() < 8)  return "Password must be at least 8 characters";
        if (userRepository.existsByEmail(email))         return "Email already registered";
        if (medicalCouncilId == null || medicalCouncilId.isBlank()) return "Medical Council ID is required";
        if (userRepository.existsByMedicalCouncilId(medicalCouncilId.toUpperCase()))
            return "Medical Council ID already registered";

        User user = new User();
        user.setName(sanitize(name));
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.DOCTOR);
        user.setMedicalCouncilId(medicalCouncilId.toUpperCase().trim());
        user.setPhone(phone);
        // Doctor profile (specialization etc.) filled in after admin approval
        userRepository.save(user);
        return "success";
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.replaceAll("<","&lt;").replaceAll(">","&gt;")
                    .replaceAll("\"","&quot;").replaceAll("'","&#x27;").trim();
    }
}
