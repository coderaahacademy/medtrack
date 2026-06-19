package com.medtrack.service;

import com.medtrack.dto.*;
import com.medtrack.entity.*;
import com.medtrack.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final MedicationRepository medicationRepository;
    public PrescriptionService(PrescriptionRepository prescriptionRepository, PatientRepository patientRepository, DoctorRepository doctorRepository,
                               VisitRepository visitRepository, MedicationRepository medicationRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
    }
    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();
        Long visitId = request.getVisitId();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found by id: " + patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found by id: " + doctorId));
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Visit not found by id: " + visitId));
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setVisit(visit);
        prescription.setStatus(request.getStatus());
        prescription.setIssueDate(request.getIssueDate());
        prescription.setNotes(request.getNotes());
        request.getItems().forEach(itemRequest -> {
            Long medicationId = itemRequest.getMedicationId();
            Medication medication = medicationRepository.findById(medicationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Medication not found by id: " + medicationId));
            PrescriptionItem item = new PrescriptionItem();
            item.setMedication(medication);
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDurationDays(itemRequest.getDurationDays());
            item.setQuantity(itemRequest.getQuantity());
            item.setInstructions(itemRequest.getInstructions());
            prescription.addItem(item);
        });
        return toResponse(prescriptionRepository.saveAndFlush(prescription));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPrescriptions(Long patientId, Pageable pageable) {
        return prescriptionRepository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setStatus(prescription.getStatus());
        response.setIssueDate(prescription.getIssueDate());
        response.setNotes(prescription.getNotes());
        response.setPatientId(prescription.getPatient().getId());
        response.setDoctorId(prescription.getDoctor().getId());
        response.setVisitId(prescription.getVisit().getId());
        List<PrescriptionItemResponse> itemResponses = prescription.getItems()
                .stream().map(this::toItemResponse).collect(Collectors.toList());
        response.setItems(itemResponses);
        response.setId(prescription.getId());
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());
        return response;
    }

    private PrescriptionItemResponse toItemResponse(PrescriptionItem prescriptionItem) {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        response.setMedicationId(prescriptionItem.getMedication().getId());
        response.setDosage(prescriptionItem.getDosage());
        response.setFrequency(prescriptionItem.getFrequency());
        response.setDurationDays(prescriptionItem.getDurationDays());
        response.setQuantity(prescriptionItem.getQuantity());
        response.setInstructions(prescriptionItem.getInstructions());
        response.setId(prescriptionItem.getId());
        response.setPrescriptionId(prescriptionItem.getPrescription().getId());
        response.setCreatedAt(prescriptionItem.getCreatedAt());
        response.setUpdatedAt(prescriptionItem.getUpdatedAt());
        return response;
    }
}
