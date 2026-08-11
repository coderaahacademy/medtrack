package com.medtrack;

import com.medtrack.dto.PrescriptionItemRequest;
import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.entity.*;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.repository.*;
import com.medtrack.service.PrescriptionAuditService;
import com.medtrack.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class PrescriptionIntegrationTest {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PrescriptionAuditRepository auditRepository;

    @Autowired
    private PrescriptionItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private PrescriptionAuditService auditService;

    private Long patientId;
    private Long doctorId;
    private Long medicationId;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        prescriptionRepository.deleteAll();
        medicationRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("patient@example.com");
        user1.setPasswordHash("hash");
        user1.setStatus(com.medtrack.enums.UserStatus.ACTIVE);
        user1 = userRepository.save(user1);

        Patient patient = new Patient();
        patient.setUser(user1);
        patient.setFullName("John Doe");
        patient = patientRepository.save(patient);
        patientId = patient.getId();

        User user2 = new User();
        user2.setEmail("doctor@example.com");
        user2.setPasswordHash("hash");
        user2.setStatus(com.medtrack.enums.UserStatus.ACTIVE);
        user2 = userRepository.save(user2);

        Doctor doctor = new Doctor();
        doctor.setUser(user2);
        doctor.setFullName("Jane Smith");
        doctor.setSpecialization("General");
        doctor.setLicenseNumber("LIC123");
        doctor = doctorRepository.save(doctor);
        doctorId = doctor.getId();

        Medication medication = new Medication();
        medication.setName("Amoxicillin");
        medication.setActive(true);
        medication = medicationRepository.save(medication);
        medicationId = medication.getId();
    }

    @Test
    void shouldRollbackWhenAuditFails() {
        // Arrange
        PrescriptionRequest request = new PrescriptionRequest();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setIssueDate(LocalDateTime.now());
        
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicationId(medicationId);
        item.setDosage("500mg");
        item.setFrequency("BID");
        item.setDurationDays(7);
        item.setQuantity(14);
        
        request.setItems(List.of(item));

        // Mock auditService to throw exception
        doThrow(new RuntimeException("Audit failed"))
                .when(auditService).recordEvent(anyLong(), any(), any(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> prescriptionService.create(request));

        // Verify rollback
        assertEquals(0, prescriptionRepository.count(), "Prescription should not be persisted");
        assertEquals(0, itemRepository.count(), "Prescription items should not be persisted");
        assertEquals(0, auditRepository.count(), "Audit should not be persisted");
    }

    @Test
    void shouldSucceedAndPersistEverything() {
        // Arrange
        PrescriptionRequest request = new PrescriptionRequest();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setIssueDate(LocalDateTime.now());
        
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicationId(medicationId);
        item.setDosage("500mg");
        item.setFrequency("BID");
        item.setDurationDays(7);
        item.setQuantity(14);
        
        request.setItems(List.of(item));

        // Act
        var response = prescriptionService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, prescriptionRepository.count());
        assertEquals(1, itemRepository.count());
        assertEquals(1, auditRepository.count());
        
        Prescription saved = prescriptionRepository.findById(response.getId()).orElseThrow();
        assertEquals(PrescriptionStatus.ISSUED, saved.getStatus());
    }
}
