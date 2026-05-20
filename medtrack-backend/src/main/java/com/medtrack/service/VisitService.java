package com.medtrack.service;

import com.medtrack.dto.VisitRequest;
import com.medtrack.dto.VisitResponse;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Visit;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public VisitService(VisitRepository visitRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public VisitResponse createVisit(VisitRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDate(request.getVisitDate());
        visit.setReason(request.getReason());
        visit.setDiagnosis(request.getDiagnosis());
        visit.setNotes(request.getNotes());

        Visit saved = visitRepository.save(visit);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> getVisitsByPatient(Long patientId) {
        return visitRepository.findByPatientId(patientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));
        return toResponse(visit);
    }

    private VisitResponse toResponse(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setPatientName(visit.getPatient().getFullName());
        response.setDoctorName(visit.getDoctor().getFullName());
        response.setVisitDate(visit.getVisitDate());
        response.setReason(visit.getReason());
        response.setDiagnosis(visit.getDiagnosis());
        response.setNotes(visit.getNotes());
        response.setCreatedAt(visit.getCreatedAt());
        return response;
    }
}
