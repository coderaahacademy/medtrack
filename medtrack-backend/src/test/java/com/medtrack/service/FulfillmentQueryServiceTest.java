package com.medtrack.service;

import com.medtrack.dto.FulfillmentDetailResponse;
import com.medtrack.dto.FulfillmentSummaryResponse;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.entity.PrescriptionItem;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import com.medtrack.repository.PrescriptionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FulfillmentQueryServiceTest {

    @Mock
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;

    @InjectMocks
    private FulfillmentQueryService queryService;

    private Pharmacy pharmacy;
    private Patient patient;
    private Prescription prescription;
    private PrescriptionFulfillment fulfillment;

    @BeforeEach
    void setUp() {
        pharmacy = new Pharmacy();
        pharmacy.setId(10L);
        pharmacy.setName("Apotheke Nord");

        patient = new Patient();
        patient.setId(20L);
        patient.setFullName("Anna Muster");
        patient.setBirthDate(LocalDate.of(1990, 5, 17));
        patient.setAllergies("Penicillin");

        prescription = new Prescription();
        prescription.setId(30L);
        prescription.setPatient(patient);
        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setIssueDate(LocalDateTime.now());

        fulfillment = new PrescriptionFulfillment();
        fulfillment.setId(40L);
        fulfillment.setPharmacy(pharmacy);
        fulfillment.setPrescription(prescription);
        fulfillment.setStatus(FulfillmentStatus.PENDING);
        fulfillment.setRequestedAt(LocalDateTime.now());
    }

    private PrescriptionItem item(Long id, Medication medication, int prescribed, int dispensed) {
        PrescriptionItem i = new PrescriptionItem();
        i.setId(id);
        i.setMedication(medication);
        i.setQuantity(prescribed);
        i.setDispensedQuantity(dispensed);
        i.setDosage("500mg");
        i.setFrequency("2x daily");
        i.setDurationDays(5);
        return i;
    }

    private Medication medication(Long id, String name) {
        Medication m = new Medication();
        m.setId(id);
        m.setName(name);
        m.setStrength("500mg");
        m.setDosageForm("Tablet");
        return m;
    }

    // =========================================================================
    // QUEUE
    // =========================================================================

    @Test
    @DisplayName("Queue without status filter uses the unfiltered repository method")
    void queueWithoutStatusUsesUnfilteredQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        when(fulfillmentRepository.findByPharmacyId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fulfillment), pageable, 1));
        when(prescriptionItemRepository.countItemsByPrescriptionIds(List.of(30L)))
                .thenReturn(List.<Object[]>of(new Object[]{30L, 2L}));

        Page<FulfillmentSummaryResponse> page = queryService.getPharmacyQueue(10L, null, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(40L, page.getContent().get(0).getId());
        assertEquals(30L, page.getContent().get(0).getPrescriptionId());
        assertEquals("Anna Muster", page.getContent().get(0).getPatientName());
        assertEquals(2, page.getContent().get(0).getItemCount());

        verify(fulfillmentRepository, never())
                .findByPharmacyIdAndStatus(anyLong(), any(FulfillmentStatus.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Queue with status filter uses the filtered repository method")
    void queueWithStatusUsesFilteredQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        when(fulfillmentRepository.findByPharmacyIdAndStatus(
                eq(10L), eq(FulfillmentStatus.PENDING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(fulfillment), pageable, 1));
        when(prescriptionItemRepository.countItemsByPrescriptionIds(List.of(30L)))
                .thenReturn(List.<Object[]>of(new Object[]{30L, 1L}));

        Page<FulfillmentSummaryResponse> page =
                queryService.getPharmacyQueue(10L, FulfillmentStatus.PENDING, pageable);

        assertEquals(1, page.getTotalElements());
        verify(fulfillmentRepository, never()).findByPharmacyId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Empty queue does not run the item-count query")
    void emptyQueueSkipsCountQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        when(fulfillmentRepository.findByPharmacyId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<FulfillmentSummaryResponse> page = queryService.getPharmacyQueue(10L, null, pageable);

        assertEquals(0, page.getTotalElements());
        verify(prescriptionItemRepository, never()).countItemsByPrescriptionIds(any());
    }

    // =========================================================================
    // DETAIL
    // =========================================================================

    @Test
    @DisplayName("Detail maps prescribed, dispensed and remaining quantity per item")
    void detailCalculatesRemainingQuantity() {
        prescription.getItems().add(item(50L, medication(60L, "Amoxicillin"), 10, 4));
        when(fulfillmentRepository.findDetailById(40L)).thenReturn(Optional.of(fulfillment));

        FulfillmentDetailResponse response = queryService.getFulfillmentDetail(40L);

        assertEquals(1, response.getItems().size());
        assertEquals(50L, response.getItems().get(0).getPrescriptionItemId());
        assertEquals("Amoxicillin", response.getItems().get(0).getMedicationName());
        assertEquals(10, response.getItems().get(0).getPrescribedQuantity());
        assertEquals(4, response.getItems().get(0).getDispensedQuantity());
        assertEquals(6, response.getItems().get(0).getRemainingQuantity());
    }

    @Test
    @DisplayName("Remaining quantity never becomes negative")
    void remainingQuantityIsNeverNegative() {
        prescription.getItems().add(item(51L, medication(61L, "Ibuprofen"), 5, 8));
        when(fulfillmentRepository.findDetailById(40L)).thenReturn(Optional.of(fulfillment));

        FulfillmentDetailResponse response = queryService.getFulfillmentDetail(40L);

        assertEquals(0, response.getItems().get(0).getRemainingQuantity());
    }

    @Test
    @DisplayName("Detail exposes patient data needed for dispensing")
    void detailExposesDispensingRelevantPatientData() {
        when(fulfillmentRepository.findDetailById(40L)).thenReturn(Optional.of(fulfillment));

        FulfillmentDetailResponse response = queryService.getFulfillmentDetail(40L);

        assertEquals(20L, response.getPatientId());
        assertEquals("Anna Muster", response.getPatientName());
        assertEquals(LocalDate.of(1990, 5, 17), response.getPatientBirthDate());
        assertEquals("Penicillin", response.getPatientAllergies());
        assertEquals("Apotheke Nord", response.getPharmacyName());
    }

    @Test
    @DisplayName("Rejection reason is only exposed for a REJECTED fulfillment")
    void rejectionReasonOnlyForRejected() {
        fulfillment.setStatus(FulfillmentStatus.ACCEPTED);
        fulfillment.setRejectionReason("stale reason");
        when(fulfillmentRepository.findDetailById(40L)).thenReturn(Optional.of(fulfillment));

        assertNull(queryService.getFulfillmentDetail(40L).getRejectionReason());

        fulfillment.setStatus(FulfillmentStatus.REJECTED);
        assertEquals("stale reason", queryService.getFulfillmentDetail(40L).getRejectionReason());
    }

    @Test
    @DisplayName("Unknown fulfillment id results in 404, not 400")
    void unknownIdReturns404() {
        when(fulfillmentRepository.findDetailById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> queryService.getFulfillmentDetail(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
