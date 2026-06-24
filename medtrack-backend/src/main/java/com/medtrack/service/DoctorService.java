package com.medtrack.service;

import com.medtrack.dto.*;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.UserRepository;
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
    public DoctorResponse create(CreateDoctorRequest request) {
        Long userId = request.getUserId();
        User user = userRepository.findByIdOrThrow(userId);
        if (doctorRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor profile already exists for user ID: " + userId);
        }
        Doctor doctor = new Doctor();
        mapRequestToEntity(request,doctor);
        boolean hasDoctorRole = user.getRoles().stream().anyMatch(r -> r.getRole() == Role.DOCTOR);
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
        Doctor doctor = doctorRepository.findByIdOrThrow(id);
        mapRequestToEntity(request,doctor);
        return toResponse(doctorRepository.saveAndFlush(doctor));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        Doctor doctor = doctorRepository.findByIdOrThrow(id);
        return toResponse(doctor);
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAll(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public MessageResponse delete(Long id) {
        Doctor doctor = doctorRepository.findByIdOrThrow(id);
        User user = doctor.getUser();
        user.getRoles().removeIf(r -> r.getRole() == Role.DOCTOR);
        patientRepository.unassignFamilyDoctor(id);
        doctorRepository.delete(doctor);
        userRepository.saveAndFlush(user);
        return new MessageResponse("Doctor ID " + id + " was successfully deleted.");
    }

    private void mapRequestToEntity(DoctorRequest request, Doctor doctor) {
        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setActive(request.isActive());
    }

    private DoctorResponse toResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setFullName(doctor.getFullName());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setPhone(doctor.getPhone());
        response.setActive(doctor.isActive());
        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());
        response.setUserId(doctor.getUser().getId());
        response.setId(doctor.getId());
        return response;
    }
}
