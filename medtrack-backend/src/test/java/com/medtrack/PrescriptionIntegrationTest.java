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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PrescriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Autowired
    private com.medtrack.service.PrescriptionSubmissionService submissionService;

    @Autowired
    private com.medtrack.security.JwtService jwtService;

    @SpyBean
    private PrescriptionAuditService auditService;

    private Long patientId;
    private Long doctorId;
    private Long medicationId;
    private String doctorToken;
    private String adminToken;
    private User userPharmacy;

    @BeforeEach
    void setUp() {
        auditRepository.deleteAll();
        fulfillmentRepository.deleteAll();
        itemRepository.deleteAll();
        prescriptionRepository.deleteAll();
        medicationRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
        pharmacyRepository.deleteAll();

        User user1 = new User();
        user1.setEmail("patient@example.com");
        user1.setPasswordHash("hash");
        user1.setStatus(com.medtrack.enums.UserStatus.ACTIVE);
        user1.addRole(com.medtrack.enums.Role.PATIENT);
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
        user2.addRole(com.medtrack.enums.Role.DOCTOR);
        user2 = userRepository.save(user2);

        Doctor doctor = new Doctor();
        doctor.setUser(user2);
        doctor.setFullName("Jane Smith");
        doctor.setSpecialization("General");
        doctor.setLicenseNumber("LIC123");
        doctor = doctorRepository.save(doctor);
        doctorId = doctor.getId();

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("hash");
        adminUser.setStatus(com.medtrack.enums.UserStatus.ACTIVE);
        adminUser.addRole(com.medtrack.enums.Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        userPharmacy = new User();
        userPharmacy.setEmail("pharmacy@example.com");
        userPharmacy.setPasswordHash("hash");
        userPharmacy.setStatus(com.medtrack.enums.UserStatus.ACTIVE);
        userPharmacy.addRole(com.medtrack.enums.Role.PHARMACY);
        userPharmacy = userRepository.save(userPharmacy);

        doctorToken = jwtService.generateToken(user2);
        adminToken = jwtService.generateToken(adminUser);

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

    @Test
    void shouldRollbackCancellationWhenAuditFails() {
        // Arrange
        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).get());
        prescription.setDoctor(doctorRepository.findById(doctorId).get());
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());
        prescription = prescriptionRepository.saveAndFlush(prescription);
        Long id = prescription.getId();

        // Mock auditService to throw exception
        doThrow(new RuntimeException("Audit failed during cancellation"))
                .when(auditService).recordEvent(eq(id), any(), any(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> prescriptionService.cancelPrescription(id, "Cancel me"));

        // Verify rollback
        Prescription notCancelled = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(PrescriptionStatus.ISSUED, notCancelled.getStatus());
        assertNull(notCancelled.getCancellationReason());
        assertNull(notCancelled.getCancelledAt());
        assertEquals(0, auditRepository.count(), "Audit should not be persisted");
    }

    @Test
    void shouldReturn200OnSuccessfulCancellationApi() throws Exception {
        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).get());
        prescription.setDoctor(doctorRepository.findById(doctorId).get());
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());
        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.CancelPrescriptionRequest request = new com.medtrack.dto.CancelPrescriptionRequest();
        request.setReason("Patient requested cancellation");

        mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/cancel")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Patient requested cancellation"));
    }

    @Test
    void shouldReturn409ForInvalidStatusTransitionApi() throws Exception {
        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).get());
        prescription.setDoctor(doctorRepository.findById(doctorId).get());
        prescription.setStatus(PrescriptionStatus.COMPLETED);
        prescription.setIssueDate(LocalDateTime.now());
        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.CancelPrescriptionRequest request = new com.medtrack.dto.CancelPrescriptionRequest();
        request.setReason("Cancel me");

        mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/cancel")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldRollbackSubmissionWhenAuditFails() {
        // Arrange
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setUser(userPharmacy);
        pharmacy.setName("Test Pharmacy");
        pharmacy.setActive(true);
        pharmacy = pharmacyRepository.save(pharmacy);
        Long pId = pharmacy.getId();

        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).get());
        prescription.setDoctor(doctorRepository.findById(doctorId).get());
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());
        prescription = prescriptionRepository.saveAndFlush(prescription);
        Long id = prescription.getId();

        // Mock auditService to throw exception
        doThrow(new RuntimeException("Audit failed during submission"))
                .when(auditService).recordEvent(eq(id), any(), any(), anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> submissionService.sendToPharmacy(id, pId));

        // Verify rollback
        Prescription notSent = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(PrescriptionStatus.ISSUED, notSent.getStatus());
        assertEquals(0, fulfillmentRepository.count(), "Fulfillment should not be persisted");
        assertEquals(0, auditRepository.count(), "Audit should not be persisted");
    }

    @Test
    void shouldSucceedSubmissionAndPersistEverything() {
        // Arrange
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setUser(userPharmacy);
        pharmacy.setName("Test Pharmacy");
        pharmacy.setActive(true);
        pharmacy = pharmacyRepository.save(pharmacy);
        Long pId = pharmacy.getId();

        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).get());
        prescription.setDoctor(doctorRepository.findById(doctorId).get());
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());
        prescription = prescriptionRepository.saveAndFlush(prescription);
        Long id = prescription.getId();

        // Act
        submissionService.sendToPharmacy(id, pId);

        // Assert
        Prescription sent = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(PrescriptionStatus.SENT_TO_PHARMACY, sent.getStatus());
        assertEquals(1, fulfillmentRepository.count());
        assertEquals(1, auditRepository.count());
    }

    @Test
    void shouldReturn409WhenUpdatingIssuedToSentToPharmacyViaGenericStatusApi()
            throws Exception {

        Prescription prescription = new Prescription();
        prescription.setPatient(
                patientRepository.findById(patientId).orElseThrow()
        );
        prescription.setDoctor(
                doctorRepository.findById(doctorId).orElseThrow()
        );
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());

        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.PrescriptionStatusUpdateRequest request =
                new com.medtrack.dto.PrescriptionStatusUpdateRequest();

        request.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);

        mockMvc.perform(
                        patch("/api/prescriptions/"
                                + prescription.getId()
                                + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_STATE_TRANSITION")
                );
    }
    @Test
    void shouldReturn409WhenUpdatingSentToPharmacyToCompletedViaGenericStatusApi()
            throws Exception {

        Prescription prescription = new Prescription();
        prescription.setPatient(
                patientRepository.findById(patientId).orElseThrow()
        );
        prescription.setDoctor(
                doctorRepository.findById(doctorId).orElseThrow()
        );
        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setIssueDate(LocalDateTime.now());

        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.PrescriptionStatusUpdateRequest request =
                new com.medtrack.dto.PrescriptionStatusUpdateRequest();

        request.setStatus(PrescriptionStatus.COMPLETED);

        mockMvc.perform(
                        patch("/api/prescriptions/"
                                + prescription.getId()
                                + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_STATE_TRANSITION")
                );
    }
    @Test
    void shouldReturn409WhenUpdatingCompletedToIssued() throws Exception {
        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).orElseThrow());
        prescription.setDoctor(doctorRepository.findById(doctorId).orElseThrow());
        prescription.setStatus(PrescriptionStatus.COMPLETED);
        prescription.setIssueDate(LocalDateTime.now());

        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.PrescriptionStatusUpdateRequest request =
                new com.medtrack.dto.PrescriptionStatusUpdateRequest();

        request.setStatus(PrescriptionStatus.ISSUED);

        mockMvc.perform(
                        patch("/api/prescriptions/" + prescription.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }

    @Test
    void shouldReturn409WhenUpdatingToSameStatus() throws Exception {
        Prescription prescription = new Prescription();
        prescription.setPatient(patientRepository.findById(patientId).orElseThrow());
        prescription.setDoctor(doctorRepository.findById(doctorId).orElseThrow());
        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssueDate(LocalDateTime.now());

        prescription = prescriptionRepository.saveAndFlush(prescription);

        com.medtrack.dto.PrescriptionStatusUpdateRequest request =
                new com.medtrack.dto.PrescriptionStatusUpdateRequest();

        request.setStatus(PrescriptionStatus.ISSUED);

        mockMvc.perform(
                        patch("/api/prescriptions/" + prescription.getId() + "/status")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
    }


}
