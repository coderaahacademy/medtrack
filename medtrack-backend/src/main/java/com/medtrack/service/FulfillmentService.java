package com.medtrack.service;
import com.medtrack.dto.FulfillmentResponse;
import com.medtrack.dto.RejectFulfillmentRequest;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import org.springframework.stereotype.Service;
import com.medtrack.entity.PharmacyInventory;
import com.medtrack.entity.PrescriptionItem;

import java.time.LocalDateTime;

@Service
public class FulfillmentService {
    private final PrescriptionFulfillmentRepository prescriptionFulfillmentRepository;
    private final InventoryRepository inventoryRepository;

    public FulfillmentService(PrescriptionFulfillmentRepository prescriptionFulfillmentRepository, InventoryRepository inventoryRepository) {
        this.prescriptionFulfillmentRepository = prescriptionFulfillmentRepository;
        this.inventoryRepository = inventoryRepository;

    }

    public FulfillmentResponse accept(Long id) {
        PrescriptionFulfillment fulfillment = getById(id);

        if (fulfillment.getStatus() != FulfillmentStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING fulfillments can be accepted. Current status: " + fulfillment.getStatus());
        }

        checkInventorySufficient(fulfillment);
        fulfillment.setStatus(FulfillmentStatus.ACCEPTED);
        fulfillment.setAcceptedAt(LocalDateTime.now());

        return toResponse(prescriptionFulfillmentRepository.saveAndFlush(fulfillment));
    }

    public FulfillmentResponse reject(Long id, RejectFulfillmentRequest request) {

        PrescriptionFulfillment fulfillment = getById(id);
        if (fulfillment.getStatus() != FulfillmentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING fulfillments can be rejected. Current status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.REJECTED);
        fulfillment.setRejectionReason(request.getRejectionReason());
        return toResponse(prescriptionFulfillmentRepository.saveAndFlush(fulfillment));


    }
    private PrescriptionFulfillment getById(Long id) {
        return prescriptionFulfillmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fulfillment not found with id " + id));
    }
    private void checkInventorySufficient(PrescriptionFulfillment fulfillment) {
        Long pharmacyId = fulfillment.getPharmacy().getId();

        for (PrescriptionItem item : fulfillment.getPrescription().getItems()) {
            Long medicationId = item.getMedication().getId();
            int required = item.getQuantity();

            PharmacyInventory inventory = inventoryRepository
                    .findByPharmacyIdAndMedicationId(pharmacyId, medicationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No inventory found for medication id " + medicationId));

            if (inventory.getQuantityAvailable() < required) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for medication id " + medicationId
                                + ". Required: " + required
                                + ", available: " + inventory.getQuantityAvailable());
            }
        }
    }
    private FulfillmentResponse toResponse(PrescriptionFulfillment fulfillment) {
        FulfillmentResponse response = new FulfillmentResponse();
        response.setId(fulfillment.getId());
        response.setPrescriptionId(fulfillment.getPrescription().getId());
        response.setPharmacyId(fulfillment.getPharmacy().getId());
        response.setStatus(fulfillment.getStatus());
        response.setAcceptedAt(fulfillment.getAcceptedAt());
        response.setRejectionReason(fulfillment.getRejectionReason());
        response.setUpdatedAt(fulfillment.getUpdatedAt());
        return response;
    }


}




