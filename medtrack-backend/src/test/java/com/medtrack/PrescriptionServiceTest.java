package com.medtrack.service;

import com.medtrack.entity.Doctor;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.Visit;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.exception.InvalidStatusTransitionException;
import com.medtrack.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
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

        // برای جلوگیری از NullPointerException در stream()
        prescription.setItems(new ArrayList<>());

        return prescription;
    }


    @Test
    void shouldAllowIssuedToSentToPharmacy() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.ISSUED);

        when(prescriptionRepository.findByIdOrThrow(1L))
                .thenReturn(prescription);

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


        when(prescriptionRepository.findByIdOrThrow(1L))
                .thenReturn(prescription);

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


        when(prescriptionRepository.findByIdOrThrow(1L))
                .thenReturn(prescription);


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


        when(prescriptionRepository.findByIdOrThrow(1L))
                .thenReturn(prescription);


        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.SENT_TO_PHARMACY
                )
        );
    }


    @Test
    void shouldRejectDraftStatus() {

        Prescription prescription =
                createPrescription(PrescriptionStatus.DRAFT);


        when(prescriptionRepository.findByIdOrThrow(1L))
                .thenReturn(prescription);


        assertThrows(
                InvalidStatusTransitionException.class,
                () -> prescriptionService.updateStatus(
                        1L,
                        PrescriptionStatus.ISSUED
                )
        );
    }
}