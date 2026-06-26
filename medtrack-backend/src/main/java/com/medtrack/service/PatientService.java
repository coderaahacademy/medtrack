package com.medtrack.service;

import com.medtrack.dto.FamilyDoctorRequest;
import com.medtrack.dto.PatientRequest;
import com.medtrack.dto.PatientResponse;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.User;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.UserRepository;
import com.medtrack.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medtrack.repository.PrescriptionRepository;
import com.medtrack.repository.VisitRepository;
import com.medtrack.dto.TimelineItemDTO;
import com.medtrack.dto.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.ArrayList;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final VisitRepository visitRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository, DoctorRepository doctorRepository, PrescriptionRepository prescriptionRepository, VisitRepository visitRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.visitRepository = visitRepository;
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (patientRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Patient already exists for user ID: " + request.getUserId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        Patient patient = new Patient();
        patient.setUser(user);
        if (request.getFamilyDoctorId() != null) {
            patient.setFamilyDoctor(doctorRepository.findById(request.getFamilyDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + request.getFamilyDoctorId())));
        }
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient saved = patientRepository.save(patient);
        return toResponse(saved);
    }

    @Transactional
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + id));

        if (request.getFamilyDoctorId() != null) {
            patient.setFamilyDoctor(doctorRepository.findById(request.getFamilyDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + request.getFamilyDoctorId())));
        } else {
            patient.setFamilyDoctor(null);
        }
        patient.setFullName(request.getFullName());
        patient.setBirthDate(request.getBirthDate());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setAllergies(request.getAllergies());
        patient.setChronicConditions(request.getChronicConditions());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient updated = patientRepository.save(patient);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + id));
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toResponse);
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
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + id));
        patientRepository.delete(patient);
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

    @Transactional(readOnly = true)
    public List<TimelineItemDTO> getPatientTimeline(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new IllegalArgumentException("Patient not found with ID: " + patientId);
        }

        List<TimelineItemDTO> timeline = new ArrayList<>();
        Pageable unpaged = PageRequest.of(0, Integer.MAX_VALUE);

        if (prescriptionRepository != null) {
            prescriptionRepository.findByPatientId(patientId, unpaged).forEach(prescription -> {
                TimelineItemDTO item = new TimelineItemDTO();
                item.setType(EventType.PRESCRIPTION);
                item.setDescription("Prescription issued");
                item.setDate(prescription.getCreatedAt());
                timeline.add(item);
            });
        }

        if (visitRepository != null) {
            visitRepository.findByPatientId(patientId, unpaged).forEach(visit -> {
                TimelineItemDTO item = new TimelineItemDTO();
                item.setType(EventType.VISIT);
                item.setDescription("Visited Doctor");
                item.setDate(visit.getVisitDate());
                timeline.add(item);
            });
        }


        timeline.sort((a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().compareTo(a.getDate());
        });

        return timeline;
    }

}
