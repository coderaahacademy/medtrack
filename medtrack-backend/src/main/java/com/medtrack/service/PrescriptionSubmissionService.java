package com.medtrack.service;

import com.medtrack.dto.SendToPharmacyResponse;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.PharmacyRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import com.medtrack.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PrescriptionSubmissionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyRepository pharmacyRepository;
    private final PrescriptionFulfillmentRepository fulfillmentRepository;
    private final PrescriptionAuditService auditService;

    public PrescriptionSubmissionService(
            PrescriptionRepository prescriptionRepository,
            PharmacyRepository pharmacyRepository,
            PrescriptionFulfillmentRepository fulfillmentRepository,
            PrescriptionAuditService auditService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public SendToPharmacyResponse sendToPharmacy(
            Long prescriptionId,
            Long pharmacyId
    ) {
        Prescription prescription = prescriptionRepository
                .findByIdForUpdate(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with id: " + prescriptionId
                ));

        Pharmacy pharmacy = pharmacyRepository
                .findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pharmacy not found with id: " + pharmacyId
                ));

        if (!pharmacy.isActive()) {
            throw new IllegalArgumentException("Cannot send prescription to an inactive pharmacy");
        }

        if (fulfillmentRepository.existsByPrescriptionId(prescriptionId)) {
            throw new InvalidStatusTransitionException(
                    "Prescription has already been sent to a pharmacy"
            );
        }

        if (prescription.getStatus() != PrescriptionStatus.ISSUED) {
            throw new InvalidStatusTransitionException(
                    "Prescription can only be sent when its status is ISSUED"
            );
        }

        PrescriptionStatus oldStatus = prescription.getStatus();
        LocalDateTime requestedAt = LocalDateTime.now();

        PrescriptionFulfillment fulfillment =
                new PrescriptionFulfillment();

        fulfillment.setPrescription(prescription);
        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(FulfillmentStatus.PENDING);
        fulfillment.setRequestedAt(requestedAt);

        prescription.setStatus(
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        prescriptionRepository.save(prescription);

        PrescriptionFulfillment savedFulfillment =
                fulfillmentRepository.save(fulfillment);

        auditService.recordEvent(
                prescription.getId(),
                oldStatus,
                PrescriptionStatus.SENT_TO_PHARMACY,
                "SYSTEM",
                "Prescription sent to pharmacy " + pharmacy.getId()
        );

        return toResponse(
                savedFulfillment,
                prescription,
                pharmacy,
                requestedAt
        );
    }

    private SendToPharmacyResponse toResponse(
            PrescriptionFulfillment fulfillment,
            Prescription prescription,
            Pharmacy pharmacy,
            LocalDateTime requestedAt
    ) {
        SendToPharmacyResponse response =
                new SendToPharmacyResponse();

        response.setFulfillmentId(fulfillment.getId());
        response.setPrescriptionId(prescription.getId());
        response.setPharmacyId(pharmacy.getId());
        response.setPrescriptionStatus(prescription.getStatus());
        response.setFulfillmentStatus(fulfillment.getStatus());
        response.setRequestedAt(requestedAt);

        return response;
    }
}
