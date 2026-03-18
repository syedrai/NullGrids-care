package com.medicare.service;

import com.medicare.entity.User;
import com.medicare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;

    public Optional<User> findByEmail(String email)  { return userRepository.findByEmail(email); }
    public User getById(Long id)                      { return userRepository.findById(id).orElse(null); }
    public List<User> getAll()                        { return userRepository.findAll(); }
    public void save(User user)                       { userRepository.save(user); }
    public long countAll()                            { return userRepository.count(); }

    public void toggleLock(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setAccountLocked(!u.isAccountLocked());
            userRepository.save(u);
        });
    }

    public List<Object[]> getPatientRegistrationsByDate(LocalDateTime since) {
        return userRepository.countPatientRegistrationsByDate(since);
    }
}
