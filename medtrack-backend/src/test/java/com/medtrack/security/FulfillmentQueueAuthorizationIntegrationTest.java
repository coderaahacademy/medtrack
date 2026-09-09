package com.medtrack.security;

import com.medtrack.entity.Doctor;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Patient;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.Prescription;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.entity.PrescriptionItem;
import com.medtrack.entity.User;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import com.medtrack.repository.DoctorRepository;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.PatientRepository;
import com.medtrack.repository.PharmacyRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import com.medtrack.repository.PrescriptionItemRepository;
import com.medtrack.repository.PrescriptionRepository;
import com.medtrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T46: Pharmacy fulfillment queue and fulfillment details.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FulfillmentQueueAuthorizationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PharmacyRepository pharmacyRepository;
    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private PrescriptionItemRepository itemRepository;
    @Autowired private PrescriptionFulfillmentRepository fulfillmentRepository;
    @Autowired private MedicationRepository medicationRepository;

    private Pharmacy pharmacyA;
    private Pharmacy pharmacyB;
    private User pharmacyUserA;
    private User pharmacyUserB;
    private User adminUser;
    private User patientUser;

    private PrescriptionFulfillment pendingA;
    private PrescriptionFulfillment acceptedA;
    private PrescriptionFulfillment fulfillmentB;

    @BeforeEach
    void setUp() {
        fulfillmentRepository.deleteAll();
        itemRepository.deleteAll();
        prescriptionRepository.deleteAll();
        medicationRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        pharmacyRepository.deleteAll();
        userRepository.deleteAll();

        pharmacyUserA = createUser("pharmacy-a@medtrack.test", Role.PHARMACY);
        pharmacyUserB = createUser("pharmacy-b@medtrack.test", Role.PHARMACY);
        adminUser = createUser("admin@medtrack.test", Role.ADMIN);
        patientUser = createUser("patient@medtrack.test", Role.PATIENT);

        pharmacyA = createPharmacy(pharmacyUserA, "Apotheke A");
        pharmacyB = createPharmacy(pharmacyUserB, "Apotheke B");

        Patient patient = createPatient(patientUser, "Anna Muster");
        User doctorUser = createUser("doctor@medtrack.test", Role.DOCTOR);
        Doctor doctor = createDoctor(doctorUser, "Dr. Bernd Arzt", "LIC-1001");
        Medication medication = createMedication("Amoxicillin");

        Prescription p1 = createPrescription(patient, doctor);
        createItem(p1, medication, 10, 4);
        Prescription p2 = createPrescription(patient, doctor);
        createItem(p2, medication, 6, 0);
        Prescription p3 = createPrescription(patient, doctor);
        createItem(p3, medication, 8, 0);

        pendingA = createFulfillment(p1, pharmacyA, FulfillmentStatus.PENDING);
        acceptedA = createFulfillment(p2, pharmacyA, FulfillmentStatus.ACCEPTED);
        fulfillmentB = createFulfillment(p3, pharmacyB, FulfillmentStatus.PENDING);
    }

    // =========================================================================
    // FIXTURES
    // =========================================================================

    private User createUser(String email, Role... roles) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setStatus(UserStatus.ACTIVE);
        for (Role role : roles) {
            user.addRole(role);
        }
        return userRepository.save(user);
    }

    private String token(User user) {
        return jwtService.generateToken(user);
    }

    private Pharmacy createPharmacy(User user, String name) {
        Pharmacy ph = new Pharmacy();
        ph.setUser(user);
        ph.setName(name);
        ph.setAddress("456 Pharmacy Ave");
        ph.setPhone("+12025550103");
        ph.setEmail(user.getEmail());
        ph.setActive(true);
        return pharmacyRepository.save(ph);
    }

    private Patient createPatient(User user, String name) {
        Patient p = new Patient();
        p.setUser(user);
        p.setFullName(name);
        p.setBirthDate(LocalDate.of(1990, 5, 17));
        p.setGender("F");
        p.setBloodGroup("O+");
        p.setAllergies("Penicillin");
        p.setPhone("+12025550101");
        p.setAddress("123 Main St");
        return patientRepository.save(p);
    }

    private Doctor createDoctor(User user, String name, String license) {
        Doctor d = new Doctor();
        d.setUser(user);
        d.setFullName(name);
        d.setLicenseNumber(license);
        d.setSpecialization("General");
        d.setPhone("+12025550102");
        d.setActive(true);
        return doctorRepository.save(d);
    }

    private Medication createMedication(String name) {
        Medication m = new Medication();
        m.setName(name);
        m.setBrand("BrandX");
        m.setGenericName("GenericX");
        m.setStrength("500mg");
        m.setDosageForm("Tablet");
        m.setActive(true);
        return medicationRepository.save(m);
    }

    private Prescription createPrescription(Patient patient, Doctor doctor) {
        Prescription p = new Prescription();
        p.setPatient(patient);
        p.setDoctor(doctor);
        p.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        p.setIssueDate(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    private PrescriptionItem createItem(Prescription prescription, Medication medication,
                                        int quantity, int dispensed) {
        PrescriptionItem i = new PrescriptionItem();
        i.setPrescription(prescription);
        i.setMedication(medication);
        i.setDosage("500mg");
        i.setFrequency("2x daily");
        i.setDurationDays(5);
        i.setQuantity(quantity);
        i.setDispensedQuantity(dispensed);
        return itemRepository.save(i);
    }

    private PrescriptionFulfillment createFulfillment(Prescription prescription, Pharmacy pharmacy,
                                                      FulfillmentStatus status) {
        PrescriptionFulfillment f = new PrescriptionFulfillment();
        f.setPrescription(prescription);
        f.setPharmacy(pharmacy);
        f.setStatus(status);
        f.setRequestedAt(LocalDateTime.now());
        return fulfillmentRepository.save(f);
    }

    // =========================================================================
    // 1. QUEUE - OWNERSHIP
    // =========================================================================

    @Nested
    @DisplayName("1. Pharmacy queue ownership")
    class QueueOwnership {

        @Test
        @DisplayName("Pharmacy reads its own queue and sees only its own fulfillments")
        void ownQueueReturnsOnlyOwnFulfillments() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[?(@.id == " + fulfillmentB.getId() + ")]").isEmpty());
        }

        @Test
        @DisplayName("Pharmacy A may not read the queue of pharmacy B")
        void foreignQueueIsForbidden() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyB.getId() + "/fulfillments")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin may inspect any pharmacy queue")
        void adminMayReadAnyQueue() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyB.getId() + "/fulfillments")
                            .header("Authorization", "Bearer " + token(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Patient may not read a pharmacy queue")
        void patientMayNotReadQueue() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .header("Authorization", "Bearer " + token(patientUser)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated queue request returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Unknown pharmacy id returns 404 for admin, not an empty page")
        void unknownPharmacyReturns404() throws Exception {
            mockMvc.perform(get("/api/pharmacies/999999/fulfillments")
                            .header("Authorization", "Bearer " + token(adminUser)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // 2. QUEUE - FILTERING, PAGINATION, ORDERING
    // =========================================================================

    @Nested
    @DisplayName("2. Pharmacy queue filtering and paging")
    class QueueFilteringAndPaging {

        @Test
        @DisplayName("Status filter returns only matching fulfillments")
        void statusFilterNarrowsResult() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("status", "ACCEPTED")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(acceptedA.getId()));
        }

        @Test
        @DisplayName("Missing status parameter returns the whole queue, not an empty page")
        void missingStatusReturnsAll() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("Invalid status value returns 400")
        void invalidStatusReturns400() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("status", "NOT_A_STATUS")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unknown sort property returns 400")
        void unknownSortPropertyReturns400() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("sort", "doesNotExist,asc")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Pagination splits the queue into pages")
        void paginationWorks() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("page", "0")
                            .param("size", "1")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.content.length()").value(1));
        }

        @Test
        @DisplayName("Ordering can be flipped from newest to oldest")
        void orderingCanBeFlipped() throws Exception {
            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("sort", "id,asc")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(pendingA.getId()));

            mockMvc.perform(get("/api/pharmacies/" + pharmacyA.getId() + "/fulfillments")
                            .param("sort", "id,desc")
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(acceptedA.getId()));
        }
    }

    // =========================================================================
    // 3. FULFILLMENT DETAIL
    // =========================================================================

    @Nested
    @DisplayName("3. Fulfillment detail")
    class FulfillmentDetail {

        @Test
        @DisplayName("Owning pharmacy sees dispensing data including remaining quantity")
        void ownerSeesDispensingData() throws Exception {
            mockMvc.perform(get("/api/fulfillments/" + pendingA.getId())
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(pendingA.getId()))
                    .andExpect(jsonPath("$.patientName").value("Anna Muster"))
                    .andExpect(jsonPath("$.patientAllergies").value("Penicillin"))
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].prescribedQuantity").value(10))
                    .andExpect(jsonPath("$.items[0].dispensedQuantity").value(4))
                    .andExpect(jsonPath("$.items[0].remainingQuantity").value(6))
                    .andExpect(jsonPath("$.items[0].medicationName").value("Amoxicillin"));
        }

        @Test
        @DisplayName("Pharmacy A cannot read pharmacy B's fulfillment by guessing the id")
        void foreignFulfillmentIsForbidden() throws Exception {
            mockMvc.perform(get("/api/fulfillments/" + fulfillmentB.getId())
                            .header("Authorization", "Bearer " + token(pharmacyUserA)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin may read any fulfillment")
        void adminMayReadAnyFulfillment() throws Exception {
            mockMvc.perform(get("/api/fulfillments/" + fulfillmentB.getId())
                            .header("Authorization", "Bearer " + token(adminUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unknown fulfillment id returns 404")
        void unknownFulfillmentReturns404() throws Exception {
            mockMvc.perform(get("/api/fulfillments/999999")
                            .header("Authorization", "Bearer " + token(adminUser)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated detail request returns 401")
        void unauthenticatedDetailReturns401() throws Exception {
            mockMvc.perform(get("/api/fulfillments/" + pendingA.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
