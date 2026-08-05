package com.medtrack.service;

import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionAudit;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.PrescriptionAuditRepository;
import com.medtrack.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrescriptionAuditService {

    private final PrescriptionAuditRepository auditRepository;
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionAuditService(PrescriptionAuditRepository auditRepository,
                                    PrescriptionRepository prescriptionRepository) {
        this.auditRepository = auditRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

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

    public List<PrescriptionAudit> getHistory(Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new ResourceNotFoundException(
                    "Prescription not found with id: " + prescriptionId);
        }
        return auditRepository.findByPrescriptionIdOrderByEventTimestampAsc(prescriptionId);
    }
}
