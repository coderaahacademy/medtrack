package com.medtrack.service;

import com.medtrack.dto.FamilyDoctorRequest;
import com.medtrack.dto.CreatePatientRequest;
import com.medtrack.dto.PatientResponse;
import com.medtrack.dto.UpdatePatientRequest;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        if (patientRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient profile already exists for user ID: " + userId);
        }
        Patient patient = new Patient();
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        boolean hasPatientRole = user.getRoles().stream().anyMatch(r -> r.getRole() == Role.PATIENT);
        if (!hasPatientRole) {
            UserRole role = new UserRole();
            role.setRole(Role.PATIENT);
            role.setUser(user);
            user.getRoles().add(role);
            userRepository.save(user);
        }
        patient.setUser(user);
        Patient savedPatient = patientRepository.save(patient);
        return toResponse(savedPatient);
    }
    @Transactional
    public PatientResponse updatePatient(Long id, UpdatePatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id));
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        Patient savedPatient = patientRepository.save(patient);
        return toResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id));
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(Pageable pageable) {
        Page<Patient> patientPage = patientRepository.findAll(pageable);
        return patientPage.map(this::toResponse);
    }

    @Transactional
    public PatientResponse updateDoctorFamily(Long id, FamilyDoctorRequest request){
        Long doctorId = request.getFamilyDoctorId();
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + id));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));
        patient.setFamilyDoctor(doctor);
        Patient savedPatient = patientRepository.saveAndFlush(patient);
        return toResponse(savedPatient);
    }

    @Transactional
    public String deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + id));
        User user = patient.getUser();
        user.getRoles().removeIf(r -> r.getRole() == Role.PATIENT);
        patientRepository.delete(patient);
        userRepository.saveAndFlush(user);
        return "Patient ID " + id + " was successfully deleted.";
    }

    private PatientResponse toResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setUserId(patient.getUser().getId());
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
        return response;
    }
}
