package com.medtrack.patient.service;

import com.medtrack.patient.api.PatientRequest;
import com.medtrack.patient.api.PatientResponse;
import com.medtrack.patient.domain.Patient;
import com.medtrack.patient.repository.PatientRepository;
import com.medtrack.user.domain.User;
import com.medtrack.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public PatientResponse createPatient(PatientRequest request) {
        validateCreatePatientRequest(request);

        if (patientRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Patient already exists for userId: " + request.getUserId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFamilyDoctorId(request.getFamilyDoctorId());
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient savedPatient = patientRepository.save(patient);

        return PatientResponse.fromPatient(savedPatient);
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientResponse::fromPatient)
                .toList();
    }

    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));

        return PatientResponse.fromPatient(patient);
    }

    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + id));

        validateUpdatePatientRequest(request);

        patient.setFamilyDoctorId(request.getFamilyDoctorId());
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient updatedPatient = patientRepository.save(patient);

        return PatientResponse.fromPatient(updatedPatient);
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found with id: " + id);
        }

        patientRepository.deleteById(id);
    }

    private void validateCreatePatientRequest(PatientRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
    }

    private void validateUpdatePatientRequest(PatientRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
    }
}