package com.medicare.service;

import com.medicare.entity.Doctor;
import com.medicare.entity.User;
import com.medicare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    @Autowired private DoctorRepository doctorRepository;

    public List<Doctor> getAll()                            { return doctorRepository.findAll(); }
    public Doctor getById(Long id)                          { return doctorRepository.findById(id).orElse(null); }
    public Optional<Doctor> getByUser(User user)            { return doctorRepository.findByUser(user); }
    public void save(Doctor doctor)                         { doctorRepository.save(doctor); }
    public void delete(Long id)                             { doctorRepository.deleteById(id); }
    public long countAll()                                  { return doctorRepository.count(); }
    public List<Doctor> search(String kw)                   { return doctorRepository.searchByNameOrSpecialization(kw); }
    public List<Doctor> getBySpecialization(String spec)    { return doctorRepository.findBySpecialization(spec); }
}
