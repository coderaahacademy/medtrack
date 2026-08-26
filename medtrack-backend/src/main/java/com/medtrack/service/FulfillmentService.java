package com.medtrack.service;

import com.medtrack.dto.CompleteFulfillmentRequest;
import com.medtrack.dto.FulfillPrescriptionItemRequest;
import com.medtrack.dto.FulfillmentResponse;
import com.medtrack.dto.RejectFulfillmentRequest;
import com.medtrack.entity.PharmacyInventory;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.entity.PrescriptionItem;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class FulfillmentService {

    private final PrescriptionFulfillmentRepository prescriptionFulfillmentRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionStatusTransitionService statusTransitionService;
    private final PrescriptionAuditService auditService;

    public FulfillmentService(
            PrescriptionFulfillmentRepository prescriptionFulfillmentRepository,
            InventoryRepository inventoryRepository,
            PrescriptionStatusTransitionService statusTransitionService,
            PrescriptionAuditService auditService) {

        this.prescriptionFulfillmentRepository = prescriptionFulfillmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.statusTransitionService = statusTransitionService;
        this.auditService = auditService;
    }

    public FulfillmentResponse accept(Long id) {

        PrescriptionFulfillment fulfillment = getById(id);

        ensurePrescriptionIsProcessable(fulfillment);

        if (fulfillment.getStatus() != FulfillmentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING fulfillments can be accepted. Current status: "
                            + fulfillment.getStatus());
        }

        checkInventorySufficient(fulfillment);

        fulfillment.setStatus(FulfillmentStatus.ACCEPTED);
        fulfillment.setAcceptedAt(LocalDateTime.now());

        return toResponse(
                prescriptionFulfillmentRepository.saveAndFlush(fulfillment)
        );
    }

    public FulfillmentResponse reject(
            Long id,
            RejectFulfillmentRequest request) {

        PrescriptionFulfillment fulfillment = getById(id);

        ensurePrescriptionIsProcessable(fulfillment);

        if (fulfillment.getStatus() != FulfillmentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING fulfillments can be rejected. Current status: "
                            + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.REJECTED);
        fulfillment.setRejectionReason(request.getRejectionReason());

        return toResponse(
                prescriptionFulfillmentRepository.saveAndFlush(fulfillment)
        );
    }

    public FulfillmentResponse preparing(Long id) {

        PrescriptionFulfillment fulfillment = getById(id);

        ensurePrescriptionIsProcessable(fulfillment);

        if (fulfillment.getStatus() != FulfillmentStatus.ACCEPTED) {
            throw new IllegalArgumentException(
                    "Only ACCEPTED fulfillments can be moved to PREPARING. Current status: "
                            + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.PREPARING);

        return toResponse(
                prescriptionFulfillmentRepository.saveAndFlush(fulfillment)
        );
    }

    public FulfillmentResponse ready(Long id) {

        PrescriptionFulfillment fulfillment = getById(id);

        ensurePrescriptionIsProcessable(fulfillment);

        if (fulfillment.getStatus() != FulfillmentStatus.PREPARING) {
            throw new IllegalArgumentException(
                    "Only PREPARING fulfillments can be moved to READY_FOR_PICKUP. Current status: "
                            + fulfillment.getStatus());
        }

        fulfillment.setStatus(FulfillmentStatus.READY_FOR_PICKUP);
        fulfillment.setReadyAt(LocalDateTime.now());

        return toResponse(
                prescriptionFulfillmentRepository.saveAndFlush(fulfillment)
        );
    }

    @Transactional
    public FulfillmentResponse completed(
            Long id,
            CompleteFulfillmentRequest request) {

        PrescriptionFulfillment fulfillment = getById(id);

        ensurePrescriptionIsProcessable(fulfillment);


        if (fulfillment.getStatus() != FulfillmentStatus.READY_FOR_PICKUP
                && fulfillment.getStatus() != FulfillmentStatus.PARTIALLY_FULFILLED) {

            if (fulfillment.getStatus() == FulfillmentStatus.COMPLETED) {
                throw new IllegalArgumentException(
                        "Fulfillment is already completed");
            }

            throw new IllegalArgumentException(
                    "Only READY_FOR_PICKUP or PARTIALLY_FULFILLED fulfillments can be completed. Current status: "
                            + fulfillment.getStatus());
        }

        Prescription prescription = fulfillment.getPrescription();

        PrescriptionStatus oldPrescriptionStatus =
                prescription.getStatus();


        applyDispensedQuantities(fulfillment, request);


        reduceInventory(fulfillment, request);

        boolean fullyDispensed = prescription.getItems()
                .stream()
                .allMatch(item ->
                        item.getDispensedQuantity() != null
                                && item.getDispensedQuantity()
                                .equals(item.getQuantity()));

        PrescriptionStatus newPrescriptionStatus =
                fullyDispensed
                        ? PrescriptionStatus.COMPLETED
                        : PrescriptionStatus.PARTIALLY_FULFILLED;

        statusTransitionService.validate(
                oldPrescriptionStatus,
                newPrescriptionStatus
        );

        prescription.setStatus(newPrescriptionStatus);

        auditService.recordEvent(
                prescription.getId(),
                oldPrescriptionStatus,
                newPrescriptionStatus,
                "SYSTEM",
                "Prescription status updated through fulfillment completion"
        );


        if (fullyDispensed) {
            fulfillment.setStatus(FulfillmentStatus.COMPLETED);
            fulfillment.setCompletedAt(LocalDateTime.now());
        } else {
            fulfillment.setStatus(FulfillmentStatus.PARTIALLY_FULFILLED);
        }

        return toResponse(
                prescriptionFulfillmentRepository.saveAndFlush(fulfillment)
        );
    }

    private void ensurePrescriptionIsProcessable(
            PrescriptionFulfillment fulfillment) {

        if (fulfillment.getPrescription().getStatus()
                == PrescriptionStatus.CANCELLED) {

            throw new InvalidStatusTransitionException(
                    "Cannot process fulfillment for a cancelled prescription"
            );
        }
    }

    private PrescriptionFulfillment getById(Long id) {

        return prescriptionFulfillmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fulfillment not found with id " + id));
    }

    private void checkInventorySufficient(
            PrescriptionFulfillment fulfillment) {

        Long pharmacyId = fulfillment.getPharmacy().getId();

        for (PrescriptionItem item :
                fulfillment.getPrescription().getItems()) {

            Long medicationId = item.getMedication().getId();

            int required = item.getQuantity();

            PharmacyInventory inventory =
                    inventoryRepository
                            .findByPharmacyIdAndMedicationId(
                                    pharmacyId,
                                    medicationId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "No inventory found for medication id "
                                                    + medicationId));

            if (inventory.getQuantityAvailable() < required) {

                throw new IllegalArgumentException(
                        "Insufficient inventory for medication id "
                                + medicationId
                                + ". Required: "
                                + required
                                + ", available: "
                                + inventory.getQuantityAvailable());
            }
        }
    }

    /**
     * Applies the quantity dispensed in the CURRENT operation.
     *
     * Example:
     *
     * prescribed = 10
     * previously dispensed = 4
     * current request = 3
     *
     * resulting dispensed quantity = 7
     */
    private void applyDispensedQuantities(
            PrescriptionFulfillment fulfillment,
            CompleteFulfillmentRequest request) {

        if (request == null || request.getItems() == null) {
            throw new IllegalArgumentException(
                    "Fulfillment items are required");
        }

        Set<Long> requestedItemIds = new HashSet<>();

        for (FulfillPrescriptionItemRequest requestItem :
                request.getItems()) {

            Long prescriptionItemId =
                    requestItem.getPrescriptionItemId();

            if (!requestedItemIds.add(prescriptionItemId)) {
                throw new IllegalArgumentException(
                        "Duplicate prescription item id: "
                                + prescriptionItemId);
            }

            PrescriptionItem item =
                    fulfillment.getPrescription()
                            .getItems()
                            .stream()
                            .filter(existingItem ->
                                    existingItem.getId()
                                            .equals(prescriptionItemId))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Prescription item does not belong to this fulfillment"));

            int currentDispensed =
                    item.getDispensedQuantity() == null
                            ? 0
                            : item.getDispensedQuantity();

            int requestedQuantity =
                    requestItem.getDispensedQuantity();

            int newTotalDispensed =
                    currentDispensed + requestedQuantity;

            if (newTotalDispensed > item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Dispensed quantity cannot exceed prescribed quantity for prescription item id "
                                + item.getId());
            }

            item.setDispensedQuantity(newTotalDispensed);
        }

        if (requestedItemIds.size()
                != fulfillment.getPrescription()
                .getItems()
                .size()) {

            throw new IllegalArgumentException(
                    "All prescription items must be included in the fulfillment request");
        }
    }

    /**
     * Reduces inventory only by the quantity dispensed
     * in the CURRENT operation.
     */
    private void reduceInventory(
            PrescriptionFulfillment fulfillment,
            CompleteFulfillmentRequest request) {

        Long pharmacyId =
                fulfillment.getPharmacy().getId();

        for (FulfillPrescriptionItemRequest requestItem :
                request.getItems()) {

            Long prescriptionItemId =
                    requestItem.getPrescriptionItemId();

            int dispensedThisTime =
                    requestItem.getDispensedQuantity();

            PrescriptionItem item =
                    fulfillment.getPrescription()
                            .getItems()
                            .stream()
                            .filter(existingItem ->
                                    existingItem.getId()
                                            .equals(prescriptionItemId))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Prescription item does not belong to this fulfillment"));

            Long medicationId =
                    item.getMedication().getId();

            PharmacyInventory inventory =
                    inventoryRepository
                            .findForUpdate(
                                    pharmacyId,
                                    medicationId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "No inventory found for medication id "
                                                    + medicationId));

            int remaining =
                    inventory.getQuantityAvailable()
                            - dispensedThisTime;

            if (remaining < 0) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for medication id "
                                + medicationId);
            }

            inventory.setQuantityAvailable(remaining);

            inventoryRepository.save(inventory);
        }
    }

    private FulfillmentResponse toResponse(
            PrescriptionFulfillment fulfillment) {

        FulfillmentResponse response =
                new FulfillmentResponse();

        response.setId(fulfillment.getId());

        response.setPrescriptionId(
                fulfillment.getPrescription().getId());

        response.setPharmacyId(
                fulfillment.getPharmacy().getId());

        response.setStatus(
                fulfillment.getStatus());

        response.setAcceptedAt(
                fulfillment.getAcceptedAt());

        response.setReadyAt(
                fulfillment.getReadyAt());

        response.setCompletedAt(
                fulfillment.getCompletedAt());

        response.setRejectionReason(
                fulfillment.getRejectionReason());

        response.setUpdatedAt(
                fulfillment.getUpdatedAt());

        return response;
    }
}