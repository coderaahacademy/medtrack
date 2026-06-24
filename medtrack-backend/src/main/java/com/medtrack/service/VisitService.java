package com.medtrack.service;

import com.medtrack.dto.*;
import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Visit;
import com.medtrack.exception.DoctorNotFoundException;
import com.medtrack.exception.PatientNotFoundException;
import com.medtrack.exception.VisitNotFoundException;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.VisitRepository;
import com.medtrack.exception.PatientNotFoundException;
import com.medtrack.exception.DoctorNotFoundException;
import com.medtrack.exception.VisitNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public VisitService(VisitRepository visitRepository,
                        PatientRepository patientRepository,
                        DoctorRepository doctorRepository) {
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public VisitResponse create(CreateVisitRequest request) {

        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found with ID: " + patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Doctor not found with ID: " + doctorId));

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
        return visitRepository.findByPatientId(id, pageable)
                .map(this::toResponse);
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

        if (visit.getPatient() != null) {
            response.setPatientId(visit.getPatient().getId());
        }

        if (visit.getDoctor() != null) {
            response.setDoctorId(visit.getDoctor().getId());
        }

        response.setId(visit.getId());
        return response;
    }

    @Transactional
    public NotesResponse addNoteToVisit(Long visitId, NotesRequest request) {

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(
                        "Visit not found with ID: " + visitId));

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
                .orElseThrow(() -> new VisitNotFoundException(
                        "Visit not found with ID: " + visitId));

        NotesResponse response = new NotesResponse();
        response.setVisitId(visit.getId());
        response.setNotes(visit.getNotes() != null ? visit.getNotes() : "");
        response.setUpdatedAt(visit.getUpdatedAt());

        return response;
    }

    @Transactional(readOnly = true)
    public VisitResponse getById(Long visitId) {

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(
                        "Visit not found with ID: " + visitId));

        return toResponse(visit);
    }
}