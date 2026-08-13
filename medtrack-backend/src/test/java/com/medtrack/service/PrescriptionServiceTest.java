package com.medtrack.service;

import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.Visit;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidCancellationException;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.exception.ResourceNotFoundException;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.PrescriptionRepository;
import com.medtrack.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PrescriptionAuditService auditService;

    @Spy
    private PrescriptionStatusTransitionService statusTransitionService;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription createPrescription(PrescriptionStatus status) {

        Patient patient = new Patient();
        patient.setId(1L);

        Doctor doctor = new Doctor();
        doctor.setId(1L);

        Visit visit = new Visit();
        visit.setId(1L);

        Prescription prescription = new Prescription();

        prescription.setId(1L);
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setVisit(visit);
        prescription.setStatus(status);

        prescription.setItems(new ArrayList<>());

        return prescription;
    }

    // ============================================================
    // STATUS TRANSITION TESTS
    // ============================================================

    @Test
    void shouldAllowIssuedToSentToPharmacy() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        var response = prescriptionService.updateStatus(
                1L,
                PrescriptionStatus.SENT_TO_PHARMACY
        );

        assertEquals(
                PrescriptionStatus.SENT_TO_PHARMACY,
                response.getStatus()
        );
    }

    @Test
    void shouldAllowSentToPharmacyToCompleted() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.SENT_TO_PHARMACY);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        var response = prescriptionService.updateStatus(
                1L,
                PrescriptionStatus.COMPLETED
        );

        assertEquals(
                PrescriptionStatus.COMPLETED,
                response.getStatus()
        );
    }

    @Test
    void shouldRejectCompletedToIssued() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.COMPLETED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.ISSUED
                )
        );
    }

    @Test
    void shouldRejectCancelledToSentToPharmacy() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.CANCELLED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.SENT_TO_PHARMACY
                )
        );
    }

    @Test
    void shouldRejectIssuedToIssued() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.ISSUED
                )
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(auditService, never())
                .recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectSentToPharmacyToSameStatus() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.SENT_TO_PHARMACY);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.SENT_TO_PHARMACY
                )
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(auditService, never())
                .recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectCompletedToSameStatus() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.COMPLETED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.COMPLETED
                )
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(auditService, never())
                .recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectCancelledToSameStatus() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.CANCELLED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.CANCELLED
                )
        );

        verify(prescriptionRepository, never())
                .save(any(Prescription.class));

        verify(auditService, never())
                .recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAllowSentToPharmacyToPartiallyFulfilled() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.SENT_TO_PHARMACY);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        var response = prescriptionService.updateStatus(
                1L,
                PrescriptionStatus.PARTIALLY_FULFILLED
        );

        assertEquals(
                PrescriptionStatus.PARTIALLY_FULFILLED,
                response.getStatus()
        );
    }

    @Test
    void shouldAllowPartiallyFulfilledToCompleted() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.PARTIALLY_FULFILLED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        var response = prescriptionService.updateStatus(
                1L,
                PrescriptionStatus.COMPLETED
        );

        assertEquals(
                PrescriptionStatus.COMPLETED,
                response.getStatus()
        );
    }

    // ============================================================
    // CANCELLATION TESTS
    // ============================================================

    @Test
    void shouldSuccessfullyCancelPrescription() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        var response = prescriptionService.cancelPrescription(
                1L,
                "Patient requested cancellation"
        );

        assertEquals(
                PrescriptionStatus.CANCELLED,
                response.getStatus()
        );

        assertEquals(
                "Patient requested cancellation",
                response.getCancellationReason()
        );

        assertNotNull(response.getCancelledAt());
    }

    @Test
    void shouldRecordAuditEntryOnCancellation() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenReturn(prescription);

        prescriptionService.cancelPrescription(
                1L,
                "Patient requested cancellation"
        );

        verify(auditService, times(1)).recordEvent(
                eq(1L),
                eq(PrescriptionStatus.ISSUED),
                eq(PrescriptionStatus.CANCELLED),
                eq("SYSTEM"),
                eq("Patient requested cancellation")
        );
    }

    @Test
    void shouldThrowWhenPrescriptionNotFound() {

        when(prescriptionRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> prescriptionService.cancelPrescription(
                        999L,
                        "Some reason"
                )
        );
    }

    @Test
    void shouldThrowWhenCancellationReasonIsBlank() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidCancellationException.class,
                () -> prescriptionService.cancelPrescription(
                        1L,
                        "   "
                )
        );
    }

    @Test
    void shouldThrowWhenCancellationReasonIsNull() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidCancellationException.class,
                () -> prescriptionService.cancelPrescription(
                        1L,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledPrescription() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.CANCELLED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.cancelPrescription(
                        1L,
                        "Patient requested cancellation"
                )
        );
    }

    @Test
    void shouldThrowWhenCancellingCompletedPrescription() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.COMPLETED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.cancelPrescription(
                        1L,
                        "Patient requested cancellation"
                )
        );
    }

    @Test
    void shouldPropagateExceptionWhenSaveFails() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(prescription));

        when(prescriptionRepository.save(any(Prescription.class)))
                .thenThrow(
                        new RuntimeException("Database connection failed")
                );

        assertThrows(
                RuntimeException.class,
                () -> prescriptionService.cancelPrescription(
                        1L,
                        "Patient requested cancellation"
                )
        );

        verify(auditService, never())
                .recordEvent(any(), any(), any(), any(), any());
    }
}