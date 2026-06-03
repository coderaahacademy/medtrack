package com.medtrack.service;

import com.medtrack.dto.NotesResponse;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Visit;
import com.medtrack.dto.CreateVisitRequest;
import com.medtrack.dto.VisitResponse;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medtrack.dto.NotesRequest;
import com.medtrack.dto.NotesResponse;


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
    public VisitResponse createVisit(CreateVisitRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + request.getDoctorId()));
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

    public VisitResponse toResponse(Visit visit) {
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

    @Transactional
    public NotesResponse addNoteToVisit(Long visitId, NotesRequest request) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with ID: " + visitId));
        visit.setNotes(request.getNotes());
        visitRepository.save(visit);

        NotesResponse response = new NotesResponse();
        response.setVisitId(visit.getId());
        response.setNotes(visit.getNotes());
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;

    }

    @Transactional(readOnly = true)
    public NotesResponse getNoteByVisitID(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with ID:" + visitId));

        NotesResponse response = new NotesResponse();
        response.setVisitId(visit.getId());
        response.setNotes(visit.getNotes() != null ? visit.getNotes() : "");
        response.setUpdatedAt(visit.getUpdatedAt());
        return response;
    }
}
