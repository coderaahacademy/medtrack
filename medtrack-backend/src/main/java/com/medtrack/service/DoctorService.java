package com.medtrack.service;

import com.medtrack.dto.*;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.UserRepository;
import com.medtrack.exception.DoctorNotFoundException;
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

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public DoctorResponse create(CreateDoctorRequest request) {

        Long userId = request.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with ID: " + userId)
                );

        if (doctorRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException(
                    "Doctor profile already exists for user ID: " + userId
            );
        }

        Doctor doctor = new Doctor();
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());

        boolean hasDoctorRole = user.getRoles()
                .stream()
                .anyMatch(r -> r.getRole() == Role.DOCTOR);

        if (!hasDoctorRole) {
            UserRole role = new UserRole();
            role.setRole(Role.DOCTOR);
            role.setUser(user);
            user.getRoles().add(role);
            userRepository.saveAndFlush(user);
        }

        doctor.setUser(user);

        return toResponse(doctorRepository.saveAndFlush(doctor));
    }

    @Transactional
    public DoctorResponse update(Long id, UpdateDoctorRequest request) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new DoctorNotFoundException("Doctor not found with ID: " + id)
                );

        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());

        return toResponse(doctorRepository.saveAndFlush(doctor));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new DoctorNotFoundException("Doctor not found with ID: " + id)
                );

        return toResponse(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAll(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public String delete(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new DoctorNotFoundException("Doctor not found with ID: " + id)
                );

        User user = doctor.getUser();

        if (user != null) {
            user.getRoles().removeIf(r -> r.getRole() == Role.DOCTOR);
            userRepository.saveAndFlush(user);
        }

        patientRepository.unassignFamilyDoctor(id);

        doctorRepository.delete(doctor);

        return "Doctor ID " + id + " was successfully deleted.";
    }

    private DoctorResponse toResponse(Doctor doctor) {

        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setFullName(doctor.getFullName());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setPhone(doctor.getPhone());
        response.setActive(doctor.isActive());
        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());

        response.setUserId(
                doctor.getUser() != null ? doctor.getUser().getId() : null
        );

        return response;
    }
}