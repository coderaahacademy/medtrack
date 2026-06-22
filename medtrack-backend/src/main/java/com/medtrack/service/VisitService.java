package com.medtrack.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medtrack.dto.NotesRequest;
import com.medtrack.dto.NotesResponse;
import org.springframework.web.server.ResponseStatusException;

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
    public VisitResponse create(CreateVisitRequest request){
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found with ID: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found with ID: " + doctorId));
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setDoctor(doctor);
        visit.setVisitDate(request.getVisitDate());
        visit.setSymptoms(request.getSymptoms());
        visit.setDiagnosis(request.getDiagnosis());
        visit.setNotes(request.getNotes());
        visit.setFollowUpDate(request.getFollowUpDate());
        return toResponse(visitRepository.saveAndFlush(visit));
    }

    @Transactional(readOnly = true)
    public Page<VisitResponse> getByPatientId(Long id, Pageable pageable) {
        return visitRepository.findByPatientId(id, pageable).map(this::toResponse);
    }

    public VisitResponse toResponse(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setVisitDate(visit.getVisitDate());
        response.setSymptoms(visit.getSymptoms());
        response.setDiagnosis(visit.getDiagnosis());
        response.setNotes(visit.getNotes());
        response.setFollowUpDate(visit.getFollowUpDate());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        response.setPatientId(visit.getPatient().getId());
        response.setDoctorId(visit.getDoctor().getId());
        response.setId(visit.getId());
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
