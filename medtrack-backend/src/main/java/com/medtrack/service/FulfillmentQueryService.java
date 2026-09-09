package com.medtrack.service;

import com.medtrack.dto.FulfillmentDetailResponse;
import com.medtrack.dto.FulfillmentItemResponse;
import com.medtrack.dto.FulfillmentSummaryResponse;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.entity.PrescriptionItem;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import com.medtrack.repository.PrescriptionItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * T46: Read-only queries for the pharmacy fulfillment work queue.
 *
 * Deliberately separate from {@link FulfillmentService}, which owns the write side
 * (state transitions, dispensing, inventory).
 */
@Service
@Transactional(readOnly = true)
public class FulfillmentQueryService {

    private final PrescriptionFulfillmentRepository fulfillmentRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;

    public FulfillmentQueryService(PrescriptionFulfillmentRepository fulfillmentRepository,
                                   PrescriptionItemRepository prescriptionItemRepository) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
    }

    /**
     * Work queue of one pharmacy. A null status means "no status filter".
     */
    public Page<FulfillmentSummaryResponse> getPharmacyQueue(Long pharmacyId,
                                                             FulfillmentStatus status,
                                                             Pageable pageable) {

        Page<PrescriptionFulfillment> page = (status == null)
                ? fulfillmentRepository.findByPharmacyId(pharmacyId, pageable)
                : fulfillmentRepository.findByPharmacyIdAndStatus(pharmacyId, status, pageable);

        Map<Long, Integer> itemCounts = loadItemCounts(page.getContent());

        return page.map(fulfillment -> toSummary(fulfillment, itemCounts));
    }

    /**
     * Full dispensing view of a single fulfillment.
     */
    public FulfillmentDetailResponse getFulfillmentDetail(Long fulfillmentId) {
        PrescriptionFulfillment fulfillment = fulfillmentRepository.findDetailById(fulfillmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "PrescriptionFulfillment not found with ID: " + fulfillmentId));

        return toDetail(fulfillment);
    }

    // ==========================================
    // MAPPING
    // ==========================================

    /**
     * Counts the items of every prescription on the current page in a single query,
     * instead of touching the lazy items collection once per row.
     */
    private Map<Long, Integer> loadItemCounts(List<PrescriptionFulfillment> fulfillments) {
        if (fulfillments.isEmpty()) {
            return Map.of();
        }

        List<Long> prescriptionIds = new ArrayList<>();
        for (PrescriptionFulfillment fulfillment : fulfillments) {
            if (fulfillment.getPrescription() != null) {
                prescriptionIds.add(fulfillment.getPrescription().getId());
            }
        }

        if (prescriptionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : prescriptionItemRepository.countItemsByPrescriptionIds(prescriptionIds)) {
            counts.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return counts;
    }

    private FulfillmentSummaryResponse toSummary(PrescriptionFulfillment fulfillment,
                                                 Map<Long, Integer> itemCounts) {

        FulfillmentSummaryResponse response = new FulfillmentSummaryResponse();
        response.setId(fulfillment.getId());
        response.setStatus(fulfillment.getStatus());
        response.setRequestedAt(fulfillment.getRequestedAt());
        response.setCreatedAt(fulfillment.getCreatedAt());
        response.setUpdatedAt(fulfillment.getUpdatedAt());

        Prescription prescription = fulfillment.getPrescription();
        if (prescription != null) {
            response.setPrescriptionId(prescription.getId());
            response.setItemCount(itemCounts.getOrDefault(prescription.getId(), 0));

            Patient patient = prescription.getPatient();
            if (patient != null) {
                response.setPatientId(patient.getId());
                response.setPatientName(patient.getFullName());
            }
        }

        return response;
    }

    private FulfillmentDetailResponse toDetail(PrescriptionFulfillment fulfillment) {
        FulfillmentDetailResponse response = new FulfillmentDetailResponse();

        response.setId(fulfillment.getId());
        response.setStatus(fulfillment.getStatus());
        response.setRequestedAt(fulfillment.getRequestedAt());
        response.setAcceptedAt(fulfillment.getAcceptedAt());
        response.setReadyAt(fulfillment.getReadyAt());
        response.setCompletedAt(fulfillment.getCompletedAt());
        response.setCreatedAt(fulfillment.getCreatedAt());
        response.setUpdatedAt(fulfillment.getUpdatedAt());

        // Only meaningful for a rejected fulfillment - do not leak a stale reason otherwise.
        if (fulfillment.getStatus() == FulfillmentStatus.REJECTED) {
            response.setRejectionReason(fulfillment.getRejectionReason());
        }

        if (fulfillment.getPharmacy() != null) {
            response.setPharmacyId(fulfillment.getPharmacy().getId());
            response.setPharmacyName(fulfillment.getPharmacy().getName());
        }

        Prescription prescription = fulfillment.getPrescription();
        if (prescription != null) {
            response.setPrescriptionId(prescription.getId());
            response.setPrescriptionStatus(prescription.getStatus());
            response.setIssueDate(prescription.getIssueDate());
            response.setPrescriptionNotes(prescription.getNotes());

            Patient patient = prescription.getPatient();
            if (patient != null) {
                response.setPatientId(patient.getId());
                response.setPatientName(patient.getFullName());
                response.setPatientBirthDate(patient.getBirthDate());
                response.setPatientAllergies(patient.getAllergies());
            }

            List<FulfillmentItemResponse> items = new ArrayList<>();
            for (PrescriptionItem item : prescription.getItems()) {
                items.add(toItem(item));
            }
            response.setItems(items);
        }

        return response;
    }

    private FulfillmentItemResponse toItem(PrescriptionItem item) {
        FulfillmentItemResponse response = new FulfillmentItemResponse();

        response.setPrescriptionItemId(item.getId());
        response.setDosage(item.getDosage());
        response.setFrequency(item.getFrequency());
        response.setDurationDays(item.getDurationDays());
        response.setInstructions(item.getInstructions());

        Medication medication = item.getMedication();
        if (medication != null) {
            response.setMedicationId(medication.getId());
            response.setMedicationName(medication.getName());
            response.setStrength(medication.getStrength());
            response.setDosageForm(medication.getDosageForm());
        }

        int prescribed = item.getQuantity() != null ? item.getQuantity() : 0;
        int dispensed = item.getDispensedQuantity() != null ? item.getDispensedQuantity() : 0;

        response.setPrescribedQuantity(prescribed);
        response.setDispensedQuantity(dispensed);
        response.setRemainingQuantity(Math.max(prescribed - dispensed, 0));

        return response;
    }
}
