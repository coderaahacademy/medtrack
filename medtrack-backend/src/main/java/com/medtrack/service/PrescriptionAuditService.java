package com.medtrack.service;

import com.medtrack.dto.PrescriptionAuditResponse;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionAudit;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.PrescriptionAuditRepository;
import com.medtrack.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionAuditService {

    private final PrescriptionAuditRepository auditRepository;
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionAuditService(PrescriptionAuditRepository auditRepository,
                                    PrescriptionRepository prescriptionRepository) {
        this.auditRepository = auditRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional
    public void recordEvent(Long prescriptionId,
                            PrescriptionStatus previousStatus,
                            PrescriptionStatus newStatus,
                            String performedBy,
                            String description) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId));

        PrescriptionAudit audit = new PrescriptionAudit(
                prescription, previousStatus, newStatus, performedBy, description);
        auditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionAuditResponse> getHistory(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new ResourceNotFoundException(
                    "Prescription not found with id: " + prescriptionId);
        }
        return auditRepository
                .findByPrescription_IdOrderByEventTimestampAsc(prescriptionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PrescriptionAuditResponse toResponse(PrescriptionAudit audit) {
        PrescriptionAuditResponse response = new PrescriptionAuditResponse();
        response.setId(audit.getId());
        response.setPrescriptionId(audit.getPrescription().getId());
        response.setPreviousStatus(audit.getPreviousStatus());
        response.setNewStatus(audit.getNewStatus());
        response.setEventTimestamp(audit.getEventTimestamp());
        response.setPerformedBy(audit.getPerformedBy());
        response.setDescription(audit.getDescription());
        return response;
    }
}
