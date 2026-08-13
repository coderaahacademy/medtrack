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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionSubmissionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Mock
    private PrescriptionAuditService auditService;

    @Mock
    private PrescriptionStatusTransitionService statusTransitionService;

    @InjectMocks
    private PrescriptionSubmissionService submissionService;

    private Prescription createPrescription(PrescriptionStatus status) {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(status);
        return prescription;
    }

    private Pharmacy createPharmacy() {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(2L);
        pharmacy.setName("Test Pharmacy");
        pharmacy.setActive(true);
        return pharmacy;
    }

    @Test
    void shouldSendIssuedPrescriptionToPharmacy() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        Pharmacy pharmacy = createPharmacy();

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(1L))
                .thenReturn(false);

        doNothing().when(statusTransitionService).validate(
                PrescriptionStatus.ISSUED,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        when(fulfillmentRepository.save(
                any(PrescriptionFulfillment.class)
        )).thenAnswer(invocation -> {
            PrescriptionFulfillment fulfillment =
                    invocation.getArgument(0);

            fulfillment.setId(10L);
            return fulfillment;
        });

        SendToPharmacyResponse response =
                submissionService.sendToPharmacy(1L, 2L);

        assertEquals(
                PrescriptionStatus.SENT_TO_PHARMACY,
                prescription.getStatus()
        );

        assertEquals(10L, response.getFulfillmentId());
        assertEquals(1L, response.getPrescriptionId());
        assertEquals(2L, response.getPharmacyId());

        assertEquals(
                PrescriptionStatus.SENT_TO_PHARMACY,
                response.getPrescriptionStatus()
        );

        assertEquals(
                FulfillmentStatus.PENDING,
                response.getFulfillmentStatus()
        );

        assertNotNull(response.getRequestedAt());

        verify(statusTransitionService).validate(
                PrescriptionStatus.ISSUED,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        verify(prescriptionRepository).save(prescription);

        verify(auditService).recordEvent(
                eq(1L),
                eq(PrescriptionStatus.ISSUED),
                eq(PrescriptionStatus.SENT_TO_PHARMACY),
                eq("SYSTEM"),
                contains("Prescription sent to pharmacy 2")
        );

        ArgumentCaptor<PrescriptionFulfillment> captor =
                ArgumentCaptor.forClass(
                        PrescriptionFulfillment.class
                );

        verify(fulfillmentRepository).save(captor.capture());

        PrescriptionFulfillment savedFulfillment =
                captor.getValue();

        assertSame(
                prescription,
                savedFulfillment.getPrescription()
        );

        assertSame(
                pharmacy,
                savedFulfillment.getPharmacy()
        );

        assertEquals(
                FulfillmentStatus.PENDING,
                savedFulfillment.getStatus()
        );

        assertNotNull(savedFulfillment.getRequestedAt());
    }

    @Test
    void shouldRejectUnknownPrescription() {
        when(prescriptionRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> submissionService.sendToPharmacy(99L, 2L)
        );

        verifyNoInteractions(
                pharmacyRepository,
                fulfillmentRepository,
                statusTransitionService,
                auditService
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));
    }

    @Test
    void shouldRejectUnknownPharmacy() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> submissionService.sendToPharmacy(1L, 99L)
        );

        verifyNoInteractions(
                fulfillmentRepository,
                statusTransitionService,
                auditService
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));
    }

    @Test
    void shouldRejectDuplicateSubmission() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        Pharmacy pharmacy = createPharmacy();

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(1L))
                .thenReturn(true);

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> submissionService.sendToPharmacy(1L, 2L)
        );

        verify(
                statusTransitionService,
                never()
        ).validate(any(), any());

        verify(
                prescriptionRepository,
                never()
        ).save(any(Prescription.class));

        verify(
                fulfillmentRepository,
                never()
        ).save(any(PrescriptionFulfillment.class));
    }

    @Test
    void shouldRejectInactivePharmacy() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        Pharmacy pharmacy = createPharmacy();
        pharmacy.setActive(false);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> submissionService.sendToPharmacy(1L, 2L)
        );

        assertEquals(
                "Cannot send prescription to an inactive pharmacy",
                exception.getMessage()
        );

        verifyNoInteractions(
                fulfillmentRepository,
                auditService,
                statusTransitionService
        );

        verify(
                prescriptionRepository,
                never()
        ).save(any(Prescription.class));
    }

    @Test
    void shouldRejectAlreadySentPrescription() {
        Prescription prescription =
                createPrescription(
                        PrescriptionStatus.SENT_TO_PHARMACY
                );

        Pharmacy pharmacy = createPharmacy();

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(1L))
                .thenReturn(false);

        doThrow(
                new InvalidStatusTransitionException(
                        "Invalid prescription status transition"
                )
        ).when(statusTransitionService).validate(
                PrescriptionStatus.SENT_TO_PHARMACY,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> submissionService.sendToPharmacy(1L, 2L)
        );

        verify(statusTransitionService).validate(
                PrescriptionStatus.SENT_TO_PHARMACY,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        verify(
                prescriptionRepository,
                never()
        ).save(any(Prescription.class));

        verify(
                fulfillmentRepository,
                never()
        ).save(any(PrescriptionFulfillment.class));

        verifyNoInteractions(auditService);
    }

    @Test
    void shouldRejectCancelledPrescription() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.CANCELLED);

        Pharmacy pharmacy = createPharmacy();

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(1L))
                .thenReturn(false);

        doThrow(
                new InvalidStatusTransitionException(
                        "Invalid prescription status transition"
                )
        ).when(statusTransitionService).validate(
                PrescriptionStatus.CANCELLED,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> submissionService.sendToPharmacy(1L, 2L)
        );

        verify(statusTransitionService).validate(
                PrescriptionStatus.CANCELLED,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        verify(
                prescriptionRepository,
                never()
        ).save(any(Prescription.class));

        verify(
                fulfillmentRepository,
                never()
        ).save(any(PrescriptionFulfillment.class));

        verifyNoInteractions(auditService);
    }
}