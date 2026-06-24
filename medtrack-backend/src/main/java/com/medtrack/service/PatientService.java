package com.medtrack.service;

import com.medtrack.dto.*;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.User;
import com.medtrack.entity.UserRole;
import com.medtrack.enums.Role;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.UserRepository;
import com.medtrack.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest request) {
        Long userId = request.getUserId();
        User user = userRepository.findByIdOrThrow(userId);
        if (patientRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient profile already exists for user ID: " + userId);
        }
        Patient patient = new Patient();
        mapRequestToEntity(request, patient);
        boolean hasPatientRole = user.getRoles().stream().anyMatch(r -> r.getRole() == Role.PATIENT);
        if (!hasPatientRole) {
            UserRole role = new UserRole();
            role.setRole(Role.PATIENT);
            role.setUser(user);
            user.getRoles().add(role);
            userRepository.saveAndFlush(user);
        }
        patient.setUser(user);
        return toResponse(patientRepository.saveAndFlush(patient));
    }
    @Transactional
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findByIdOrThrow(id);
        mapRequestToEntity(request, patient);
        return toResponse(patientRepository.saveAndFlush(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return toResponse(patientRepository.findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public PatientResponse updateFamilyDoctor(Long id, FamilyDoctorRequest request){
        Long doctorId = request.getFamilyDoctorId();
        Patient patient = patientRepository.findByIdOrThrow(id);
        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);
        patient.setFamilyDoctor(doctor);
        return toResponse(patientRepository.saveAndFlush(patient));
    }

    @Transactional
    public MessageResponse delete(Long id) {
        Patient patient = patientRepository.findByIdOrThrow(id);
        User user = patient.getUser();
        user.getRoles().removeIf(r -> r.getRole() == Role.PATIENT);
        patientRepository.delete(patient);
        userRepository.saveAndFlush(user);
        return new MessageResponse("Patient ID " + id + " was successfully deleted.");
    }

    private void mapRequestToEntity(PatientRequest request, Patient patient) {
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
    }

    private PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        if (patient.getFamilyDoctor() != null) {
            response.setFamilyDoctorId(patient.getFamilyDoctor().getId());
        }
        response.setFullName(patient.getFullName());
        response.setBirthDate(patient.getBirthDate());
        response.setGender(patient.getGender());
        response.setBloodGroup(patient.getBloodGroup());
        response.setAllergies(patient.getAllergies());
        response.setChronicConditions(patient.getChronicConditions());
        response.setPhone(patient.getPhone());
        response.setAddress(patient.getAddress());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        response.setUserId(patient.getUser().getId());
        response.setId(patient.getId());
        return response;
    }
}
