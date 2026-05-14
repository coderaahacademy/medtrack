package com.medtrack.doctor.service;

import com.medtrack.doctor.dto.*;
import com.medtrack.doctor.entity.Doctor;
import com.medtrack.doctor.repository.DoctorRepository;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.patient.repository.PatientRepository;
import com.medtrack.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request){
        Long userId = request.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        if (doctorRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor profile already exists for user ID: " + userId);
        }
        Doctor doctor = new Doctor();
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());
        boolean hasDoctorRole = user.getRoles().stream().anyMatch(r -> r.getRole() == Role.DOCTOR);
        if (!hasDoctorRole) {
            UserRole role = new UserRole();
            role.setRole(Role.DOCTOR);
            role.setUser(user);
            user.getRoles().add(role);
            userRepository.save(user);
        }
        doctor.setUser(user);
        userRepository.save(user);
        Doctor savedDoctor = doctorRepository.save(doctor);
        return toResponse(savedDoctor);
    }
    @Transactional
    public DoctorResponse updateDoctor(Long id ,UpdateDoctorRequest request){
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id));
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());
        Doctor savedDoctor = doctorRepository.save(doctor);
        return toResponse(savedDoctor);
    }
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id){
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id));
        return toResponse(doctor);
    }
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable){
        Page<Doctor> doctorPage = doctorRepository.findAll(pageable);
        return doctorPage.map(this::toResponse);
    }
    @Transactional
    public String deleteDoctor(Long id){
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + id));
        patientRepository.unassignFamilyDoctor(id);
        User user = doctor.getUser();
        user.getRoles().removeIf(r -> r.getRole() == Role.DOCTOR);
        doctorRepository.delete(doctor);
        userRepository.saveAndFlush(user);
        return "Doctor ID " + id + " was successfully deleted.";
    }
    private DoctorResponse toResponse(Doctor doctor){
        DoctorResponse response = new DoctorResponse();
        response.setFullName(doctor.getFullName());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setPhone(doctor.getPhone());
        response.setActive(doctor.isActive());
        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());
        response.setId(doctor.getId());
        response.setUserId(doctor.getUser().getId());
        return response;
    }
}
