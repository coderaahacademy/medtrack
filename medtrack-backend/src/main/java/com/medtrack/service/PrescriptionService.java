package com.medtrack.service;

import com.medtrack.dto.PrescriptionItemResponse;
import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.entity.*;
import com.medtrack.exception.InvalidCancellationException;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import java.util.HashSet;
import java.util.Set;
import com.medtrack.dto.PrescriptionItemRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private final PrescriptionAuditService auditService;
    private final PrescriptionStatusTransitionService statusTransitionService;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final MedicationRepository medicationRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               VisitRepository visitRepository,
                               MedicationRepository medicationRepository,
                               PrescriptionAuditService auditService,
                               PrescriptionStatusTransitionService statusTransitionService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
        this.auditService = auditService;
        this.statusTransitionService = statusTransitionService;
    }

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        Set<Long> medicationIds = new HashSet<>();

        for (PrescriptionItemRequest item : request.getItems()) {
            if (!medicationIds.add(item.getMedicationId())) {
                throw new IllegalArgumentException(
                        "Duplicate medications are not allowed."
                );
            }
        }
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();

        Patient patient = patientRepository.findByIdOrThrow(patientId);
        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);
        Visit visit = null;

        if (request.getVisitId() != null) {
            visit = visitRepository.findByIdOrThrow(request.getVisitId());

            if (!visit.getPatient().getId().equals(patientId)) {
                throw new IllegalArgumentException(
                        "Visit does not belong to the specified patient."
                );
            }

            if (!visit.getDoctor().getId().equals(doctorId)) {
                throw new IllegalArgumentException(
                        "Visit does not belong to the specified doctor."
                );
            }
        }

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setVisit(visit);
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(request.getIssueDate());
        prescription.setNotes(request.getNotes());
        request.getItems().forEach(itemRequest -> {
            Long medicationId = itemRequest.getMedicationId();
            Medication medication = medicationRepository.findByIdOrThrow(medicationId);

            if (!medication.isActive()) {
                throw new IllegalArgumentException(
                        "Medication is inactive."
                );
            }
            PrescriptionItem item = new PrescriptionItem();
            item.setMedication(medication);
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDurationDays(itemRequest.getDurationDays());
            item.setQuantity(itemRequest.getQuantity());
            item.setInstructions(itemRequest.getInstructions());

            prescription.addItem(item);
        });
        Prescription saved = prescriptionRepository.saveAndFlush(prescription);
        auditService.recordEvent(saved.getId(), null, saved.getStatus(), "SYSTEM", "Prescription created");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPrescriptions(Long patientId, Pageable pageable) {
        return prescriptionRepository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getAllPrescriptions(Pageable pageable) {
        Page<Prescription> prescriptions = prescriptionRepository.findAll(pageable);
        return prescriptions.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(Long id) {
        return toResponse(prescriptionRepository.findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPatientPrescriptions(Long patientId, PrescriptionStatus status, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        Page<Prescription> prescriptions;
        if (status != null) {
            prescriptions = prescriptionRepository.findAllByPatientIdAndStatus(patientId, status, pageable);
        } else {
            prescriptions = prescriptionRepository.findAllByPatientId(patientId, pageable);
        }
        return prescriptions.map(this::toResponse);
    }

    @Transactional
    public PrescriptionResponse updateStatus(Long id, PrescriptionStatus newStatus) {
        Prescription prescription = prescriptionRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + id
                ));

        if (newStatus == PrescriptionStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Use /cancel endpoint to cancel a prescription"
            );
        }

        if (prescription.getStatus() == PrescriptionStatus.ISSUED
                && newStatus == PrescriptionStatus.SENT_TO_PHARMACY) {
            throw new InvalidStatusTransitionException(
                    "Use the send-to-pharmacy workflow to send a prescription"
            );
        }

        if (prescription.getStatus() == PrescriptionStatus.SENT_TO_PHARMACY
                && newStatus == PrescriptionStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                    "Prescription completion must go through the fulfillment workflow"
            );
        }

        statusTransitionService.validate(
                prescription.getStatus(),
                newStatus
        );

        PrescriptionStatus oldStatus = prescription.getStatus();
        prescription.setStatus(newStatus);

        Prescription saved = prescriptionRepository.save(prescription);

        auditService.recordEvent(
                saved.getId(),
                oldStatus,
                newStatus,
                "SYSTEM",
                "Status updated"
        );

        return toResponse(saved);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setStatus(prescription.getStatus());
        response.setIssueDate(prescription.getIssueDate());
        response.setNotes(prescription.getNotes());
        response.setPatientId(prescription.getPatient().getId());
        response.setDoctorId(prescription.getDoctor().getId());
        response.setVisitId(
                prescription.getVisit() != null
                        ? prescription.getVisit().getId()
                        : null
        );

        List<PrescriptionItemResponse> itemResponses = prescription.getItems()
                .stream().map(this::toItemResponse).collect(Collectors.toList());
        response.setItems(itemResponses);
        response.setId(prescription.getId());
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());
        response.setCancellationReason(prescription.getCancellationReason());
        response.setCancelledAt(prescription.getCancelledAt());
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

    @Transactional
    public PrescriptionResponse cancelPrescription(Long id, String reason) {
        Prescription prescription = prescriptionRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + id
                ));

        if (reason == null || reason.isBlank()) {
            throw new InvalidCancellationException("Cancellation reason is required");
        }

        PrescriptionStatus oldStatus = prescription.getStatus();

        statusTransitionService.validate(
                prescription.getStatus(),
                PrescriptionStatus.CANCELLED
        );

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescription.setCancellationReason(reason);
        prescription.setCancelledAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(prescription);

        auditService.recordEvent(saved.getId(), oldStatus, PrescriptionStatus.CANCELLED, "SYSTEM", reason);

        return toResponse(saved);
    }

}
