package com.medtrack.visit.service;

import com.medtrack.doctor.entity.Doctor;
import com.medtrack.doctor.repository.DoctorRepository;
import com.medtrack.patient.entity.Patient;
import com.medtrack.patient.repository.PatientRepository;
import com.medtrack.visit.dto.*;
import com.medtrack.visit.entity.Visit;
import com.medtrack.visit.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

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
    public VisitResponse createVisit(CreateVisitRequest request){
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found with ID: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: " + doctorId));
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDate(request.getVisitDate());
        visit.setSymptoms(request.getSymptoms());
        visit.setDiagnosis(request.getDiagnosis());
        visit.setNotes(request.getNotes());
        visit.setFollowUpDate(request.getFollowUpDate());
        Visit savedVisit = visitRepository.saveAndFlush(visit);
        return toResponse(savedVisit);
    }

    @Transactional(readOnly = true)
    public Page<VisitResponse> getVisitsByPatientId(Long id, Pageable pageable) {
        Page<Visit> visitPage = visitRepository.findByPatientId(id, pageable);
        return visitPage.map(this::toResponse);
    }

    public VisitResponse toResponse(Visit visit){
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setVisitDate(visit.getVisitDate());
        response.setSymptoms(visit.getSymptoms());
        response.setDiagnosis(visit.getDiagnosis());
        response.setNotes(visit.getNotes());
        response.setFollowUpDate(visit.getFollowUpDate());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;
    }
}
