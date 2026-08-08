package com.medtrack.service;

import com.medtrack.dto.PrescriptionItemResponse;
import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.entity.*;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private final PrescriptionAuditService auditService;
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
                               PrescriptionAuditService auditService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.visitRepository = visitRepository;
        this.medicationRepository = medicationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PrescriptionResponse create(PrescriptionRequest request) {
        Long patientId = request.getPatientId();
        Long doctorId = request.getDoctorId();

        Patient patient = patientRepository.findByIdOrThrow(patientId);
        Doctor doctor = doctorRepository.findByIdOrThrow(doctorId);
        Visit visit = null;

        if (request.getVisitId() != null) {
            visit = visitRepository.findByIdOrThrow(request.getVisitId());
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
        Prescription prescription = prescriptionRepository.findByIdOrThrow(id);
        validateStatusTransition(prescription.getStatus(), newStatus);
        PrescriptionStatus oldStatus = prescription.getStatus();
        prescription.setStatus(newStatus);
        Prescription saved = prescriptionRepository.save(prescription);
        auditService.recordEvent(saved.getId(), oldStatus, newStatus, "SYSTEM", "Status updated");
        return toResponse(saved);
    }

    private void validateStatusTransition(PrescriptionStatus currentStatus, PrescriptionStatus newStatus) {
        if (currentStatus == PrescriptionStatus.COMPLETED ||
                currentStatus == PrescriptionStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot change status from " + currentStatus);
        }
        switch (currentStatus) {
            case ISSUED:
                if (newStatus != PrescriptionStatus.SENT_TO_PHARMACY &&
                        newStatus != PrescriptionStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException("Invalid transition from ISSUED to " + newStatus);
                }
                break;
            case SENT_TO_PHARMACY:
                if (newStatus != PrescriptionStatus.PARTIALLY_FULFILLED &&
                        newStatus != PrescriptionStatus.CANCELLED &&
                        newStatus != PrescriptionStatus.COMPLETED) {
                    throw new InvalidStatusTransitionException("Invalid transition from SENT_TO_PHARMACY to " + newStatus);
                }
                break;
            case PARTIALLY_FULFILLED:
                if (newStatus != PrescriptionStatus.COMPLETED &&
                        newStatus != PrescriptionStatus.CANCELLED) {
                    throw new InvalidStatusTransitionException("Invalid transition from PARTIALLY_FULFILLED to " + newStatus);
                }
                break;
            default:
                throw new InvalidStatusTransitionException("Unsupported transition from " + currentStatus);
        }
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
