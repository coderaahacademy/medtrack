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

        verify(prescriptionRepository).save(prescription);

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
                fulfillmentRepository
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

        verifyNoInteractions(fulfillmentRepository);

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

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(fulfillmentRepository, never())
                .save(any(PrescriptionFulfillment.class));
    }

    @Test
    void shouldRejectPrescriptionWhenStatusIsNotIssued() {
        Prescription prescription =
                createPrescription(PrescriptionStatus.COMPLETED);

        Pharmacy pharmacy = createPharmacy();

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(pharmacyRepository.findById(2L))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(1L))
                .thenReturn(false);

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> submissionService.sendToPharmacy(1L, 2L)
        );

        assertEquals(
                PrescriptionStatus.COMPLETED,
                prescription.getStatus()
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(fulfillmentRepository, never())
                .save(any(PrescriptionFulfillment.class));
    }
}
