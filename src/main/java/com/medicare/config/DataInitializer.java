package com.medicare.config;

import com.medicare.entity.Doctor;
import com.medicare.entity.User;
import com.medicare.repository.DoctorRepository;
import com.medicare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDoctors();
    }

    private void seedAdmin() {
        // Migrate old email if exists
        userRepository.findByEmail("admin@medicare.com").ifPresent(old -> {
            old.setEmail("admin@nullgrids-care.com");
            userRepository.save(old);
        });
        // Always ensure admin exists with correct password
        userRepository.findByEmail("admin@nullgrids-care.com").ifPresentOrElse(a -> {
            a.setPassword(passwordEncoder.encode("Admin@123"));
            a.setAccountLocked(false);
            a.setFailedLoginAttempts(0);
            a.setLockTime(null);
            a.setEnabled(true);
            userRepository.save(a);
            System.out.println("Admin password reset: admin@nullgrids-care.com / Admin@123");
        }, () -> {
            User a = new User();
            a.setName("System Admin"); a.setEmail("admin@nullgrids-care.com");
            a.setPassword(passwordEncoder.encode("Admin@123"));
            a.setRole(User.Role.ADMIN);
            userRepository.save(a);
            System.out.println("Admin seeded: admin@nullgrids-care.com / Admin@123");
        });
    }

    private void seedDoctors() {
        Object[][] doctors = {
            {"Dr. Rajesh Kumar",  "rajesh@medicare.com",  "MCI-2024-001", "Cardiologist",       12, "Mon, Wed, Fri",           "10:00 AM - 3:00 PM", "9876541001", "Senior cardiologist with 12 years in interventional cardiology."},
            {"Dr. Priya Sharma",  "priya@medicare.com",   "MCI-2024-002", "Dermatologist",        8, "Tue, Thu, Sat",           "11:00 AM - 4:00 PM", "9876541002", "Expert in skin conditions and cosmetic dermatology."},
            {"Dr. Arun Mehta",    "arun@medicare.com",    "MCI-2024-003", "General Physician",   15, "Mon,Tue,Wed,Thu,Fri",     "09:00 AM - 2:00 PM", "9876541003", "General medicine specialist."},
            {"Dr. Kavitha Nair",  "kavitha@medicare.com", "MCI-2024-004", "Pediatrician",        10, "Mon, Wed, Fri",           "10:00 AM - 4:00 PM", "9876541004", "Child health specialist."},
            {"Dr. Suresh Babu",   "suresh@medicare.com",  "MCI-2024-005", "Orthopedic",           9, "Tue, Thu",               "02:00 PM - 6:00 PM", "9876541005", "Orthopedic surgeon specializing in joint replacements."},
            {"Dr. Anitha Rajan",  "anitha@medicare.com",  "MCI-2024-006", "Neurologist",         11, "Mon, Thu, Sat",           "10:00 AM - 3:00 PM", "9876541006", "Neurologist with expertise in migraines and epilepsy."},
        };

        for (Object[] d : doctors) {
            if (userRepository.findByEmail((String)d[1]).isEmpty()) {
                User u = new User();
                u.setName((String)d[0]); u.setEmail((String)d[1]);
                u.setPassword(passwordEncoder.encode("Doctor@123"));
                u.setRole(User.Role.DOCTOR);
                u.setMedicalCouncilId((String)d[2]);
                userRepository.save(u);

                Doctor doc = new Doctor();
                doc.setUser(u); doc.setSpecialization((String)d[3]);
                doc.setExperienceYears((int)d[4]); doc.setAvailableDays((String)d[5]);
                doc.setAvailableTime((String)d[6]); doc.setPhone((String)d[7]);
                doc.setBio((String)d[8]);
                doctorRepository.save(doc);
                System.out.println("✅ Doctor seeded: " + d[1] + " / Doctor@123  MCI:" + d[2]);
            }
        }
    }
}
