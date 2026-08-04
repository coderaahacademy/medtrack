package com.medtrack.service;

import com.medtrack.dto.PrescriptionAuditHistoryResponse;
import com.medtrack.entity.PrescriptionAuditHistory;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.PrescriptionAuditHistoryRepository;
import com.medtrack.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionAuditHistoryService {

    private final PrescriptionAuditHistoryRepository auditRepository;
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionAuditHistoryService(
            PrescriptionAuditHistoryRepository auditRepository,
            PrescriptionRepository prescriptionRepository) {
        this.auditRepository = auditRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional
    public void recordAudit(Long prescriptionId,
                            PrescriptionStatus previousStatus,
                            PrescriptionStatus newStatus,
                            String description) {
        PrescriptionAuditHistory audit = new PrescriptionAuditHistory();
        audit.setPrescriptionId(prescriptionId);
        audit.setPreviousStatus(previousStatus);
        audit.setNewStatus(newStatus);
        audit.setDescription(description);
        auditRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionAuditHistoryResponse> getAuditHistory(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new ResourceNotFoundException(
                    "Prescription not found with id: " + prescriptionId);
        }
        return auditRepository
                .findByPrescriptionIdOrderByEventTimestampAsc(prescriptionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PrescriptionAuditHistoryResponse toResponse(PrescriptionAuditHistory audit) {
        PrescriptionAuditHistoryResponse response = new PrescriptionAuditHistoryResponse();
        response.setId(audit.getId());
        response.setPrescriptionId(audit.getPrescriptionId());
        response.setPreviousStatus(audit.getPreviousStatus());
        response.setNewStatus(audit.getNewStatus());
        response.setEventTimestamp(audit.getEventTimestamp());
        response.setPerformedBy(audit.getPerformedBy());
        response.setDescription(audit.getDescription());
        return response;
    }
}
