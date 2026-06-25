package com.medtrack.service;

import com.medtrack.dto.PrescriptionItemResponse;
import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.entity.*;
import com.medtrack.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Patient patient = patientRepository.findByIdOrThrow(patientId);
        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);
        Visit visit = visitRepository.findByIdOrThrow(visitId);
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setVisit(visit);
        prescription.setStatus(request.getStatus());
        prescription.setIssueDate(request.getIssueDate());
        prescription.setNotes(request.getNotes());
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Prescription must contain at least one item"
            );
        }
        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            Long medicationId = itemRequest.getMedicationId();
            Medication medication = medicationRepository.findByIdOrThrow(medicationId);
            PrescriptionItem item = new PrescriptionItem();
            item.setMedication(medication);
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDurationDays(itemRequest.getDurationDays());
            item.setQuantity(itemRequest.getQuantity());
            item.setInstructions(itemRequest.getInstructions());
            prescription.addItem(item);
        };
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
        if (prescription.getPatient() != null) {
            response.setPatientId(prescription.getPatient().getId());
        }

        if (prescription.getDoctor() != null) {
            response.setDoctorId(prescription.getDoctor().getId());
        }

        if (prescription.getVisit() != null) {
            response.setVisitId(prescription.getVisit().getId());
        }
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

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getAllPrescriptions (Pageable pageable) {
        Page<Prescription> prescriptions = prescriptionRepository.findAll(pageable);
        return prescriptions.map(this::toResponse);
    }
}
