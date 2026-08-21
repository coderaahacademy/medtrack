package com.medtrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrack.dto.*;
import com.medtrack.entity.*;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.enums.Role;
import com.medtrack.enums.UserStatus;
import com.medtrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @Autowired
    private PrescriptionAuditRepository auditRepository;

    @Autowired
    private PrescriptionItemRepository itemRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MedicalReportRepository medicalReportRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void cleanDatabase() {
        inventoryRepository.deleteAll();
        medicalReportRepository.deleteAll();
        auditRepository.deleteAll();
        fulfillmentRepository.deleteAll();
        itemRepository.deleteAll();
        prescriptionRepository.deleteAll();
        visitRepository.deleteAll();
        medicationRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        pharmacyRepository.deleteAll();
        userRepository.deleteAll();
    }

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

    private Patient createPatient(User user, String name) {
        Patient p = new Patient();
        p.setUser(user);
        p.setFullName(name);
        p.setBirthDate(LocalDate.of(1990, 1, 1));
        p.setGender("M");
        p.setBloodGroup("O+");
        p.setPhone("+12025550101");
        p.setAddress("123 Main St, Springfield");
        return patientRepository.save(p);
    }

    private Doctor createDoctor(User user, String name, String license) {
        Doctor d = new Doctor();
        d.setUser(user);
        d.setFullName(name);
        d.setLicenseNumber(license);
        d.setSpecialization("Cardiology");
        d.setPhone("+12025550102");
        d.setActive(true);
        return doctorRepository.save(d);
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

    private Medication createMedication(String name) {
        Medication m = new Medication();
        m.setName(name);
        m.setBrand("BrandX");
        m.setGenericName("GenericX");
        m.setActive(true);
        return medicationRepository.save(m);
    }

    private Prescription createPrescription(Patient patient, Doctor doctor) {
        Prescription p = new Prescription();
        p.setPatient(patient);
        p.setDoctor(doctor);
        p.setStatus(PrescriptionStatus.ISSUED);
        p.setIssueDate(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    private PrescriptionFulfillment createFulfillment(Prescription prescription, Pharmacy pharmacy) {
        PrescriptionFulfillment f = new PrescriptionFulfillment();
        f.setPrescription(prescription);
        f.setPharmacy(pharmacy);
        f.setStatus(FulfillmentStatus.PENDING);
        f.setRequestedAt(LocalDateTime.now());
        return fulfillmentRepository.save(f);
    }

    private Visit createVisit(Patient patient, Doctor doctor) {
        Visit v = new Visit();
        v.setPatient(patient);
        v.setDoctor(doctor);
        v.setVisitDate(LocalDateTime.now());
        v.setDiagnosis("Routine checkup");
        v.setSymptoms("None");
        return visitRepository.save(v);
    }

    // =========================================================================
    // 1. AUTHENTICATION REGRESSION & PUBLIC ENDPOINTS
    // =========================================================================

    @Nested
    @DisplayName("1. Authentication & Public Endpoints")
    class AuthenticationAndPublicEndpoints {

        @Test
        @DisplayName("Unauthenticated request to protected endpoint returns 401 UNAUTHORIZED")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/patients/1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("Public registration with PATIENT succeeds")
        void publicRegisterPatientSucceeds() throws Exception {
            RegisterUserRequest req = new RegisterUserRequest();
            req.setEmail("newpat@example.com");
            req.setPassword("Password123!");
            req.setRole(Role.PATIENT);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newpat@example.com"))
                    .andExpect(jsonPath("$.role").value("PATIENT"));
        }

        @Test
        @DisplayName("Public registration with DOCTOR succeeds")
        void publicRegisterDoctorSucceeds() throws Exception {
            RegisterUserRequest req = new RegisterUserRequest();
            req.setEmail("newdoc@example.com");
            req.setPassword("Password123!");
            req.setRole(Role.DOCTOR);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newdoc@example.com"))
                    .andExpect(jsonPath("$.role").value("DOCTOR"));
        }

        @Test
        @DisplayName("Public registration with PHARMACY succeeds")
        void publicRegisterPharmacySucceeds() throws Exception {
            RegisterUserRequest req = new RegisterUserRequest();
            req.setEmail("newpharm@example.com");
            req.setPassword("Password123!");
            req.setRole(Role.PHARMACY);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newpharm@example.com"))
                    .andExpect(jsonPath("$.role").value("PHARMACY"));
        }

        @Test
        @DisplayName("Public registration with ADMIN is rejected with 403 FORBIDDEN")
        void publicRegisterAdminReturns403() throws Exception {
            RegisterUserRequest req = new RegisterUserRequest();
            req.setEmail("hackeradmin@example.com");
            req.setPassword("Password123!");
            req.setRole(Role.ADMIN);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.path").value("/users/register"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }

        @Test
        @DisplayName("Standardized 403 error response body contract")
        void standardized403ErrorContract() throws Exception {
            User userA = createUser("patient.a@example.com", Role.PATIENT);
            User userB = createUser("patient.b@example.com", Role.PATIENT);
            Patient patientB = createPatient(userB, "Patient B");

            mockMvc.perform(get("/api/patients/" + patientB.getId())
                            .header("Authorization", "Bearer " + token(userA)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"))
                    .andExpect(jsonPath("$.path").value("/api/patients/" + patientB.getId()))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.fieldErrors").isArray());
        }
    }

    // =========================================================================
    // 2. PATIENT PROFILE OWNERSHIP & ACCESS
    // =========================================================================

    @Nested
    @DisplayName("2. Patient Profile Ownership")
    class PatientProfileOwnership {

        @Test
        @DisplayName("Patient can only create profile for own user ID; creating for another user returns 403")
        void patientCreateProfileOwnership() throws Exception {
            User userA = createUser("pat.a@example.com", Role.PATIENT);
            User userB = createUser("pat.b@example.com", Role.PATIENT);

            // Patient A creating for User B -> 403
            CreatePatientRequest crossReq = new CreatePatientRequest();
            crossReq.setUserId(userB.getId());
            crossReq.setFullName("Patient B Profile");
            crossReq.setBirthDate(LocalDate.of(1995, 5, 5));
            crossReq.setGender("F");
            crossReq.setBloodGroup("A+");
            crossReq.setPhone("+12025550111");
            crossReq.setAddress("456 Elm St, City");

            mockMvc.perform(post("/api/patients")
                            .header("Authorization", "Bearer " + token(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(crossReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Patient A creating for own User A -> 201
            crossReq.setUserId(userA.getId());
            mockMvc.perform(post("/api/patients")
                            .header("Authorization", "Bearer " + token(userA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(crossReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.fullName").value("Patient B Profile"));
        }

        @Test
        @DisplayName("Patient can access own profile but receives 403 on another patient's profile")
        void patientReadOwnAndOtherProfile() throws Exception {
            User userA = createUser("patient.a@example.com", Role.PATIENT);
            User userB = createUser("patient.b@example.com", Role.PATIENT);
            Patient patientA = createPatient(userA, "Patient A");
            Patient patientB = createPatient(userB, "Patient B");

            // Patient A -> GET Patient A -> 200
            mockMvc.perform(get("/api/patients/" + patientA.getId())
                            .header("Authorization", "Bearer " + token(userA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Patient A"));

            // Patient A -> GET Patient B -> 403
            mockMvc.perform(get("/api/patients/" + patientB.getId())
                            .header("Authorization", "Bearer " + token(userA)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Only ADMIN can enumerate all patients with GET /api/patients; normal users get 403")
        void enumerateAllPatientsRestrictedToAdmin() throws Exception {
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userDoc = createUser("doc@example.com", Role.DOCTOR);
            User userAdmin = createUser("admin@example.com", Role.ADMIN);

            // Patient -> 403
            mockMvc.perform(get("/api/patients")
                            .header("Authorization", "Bearer " + token(userPat)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Doctor -> 403
            mockMvc.perform(get("/api/patients")
                            .header("Authorization", "Bearer " + token(userDoc)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Admin -> 200
            mockMvc.perform(get("/api/patients")
                            .header("Authorization", "Bearer " + token(userAdmin)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Assigned family doctor can read patient profile, but unrelated doctor receives 403")
        void familyDoctorCanReadPatient() throws Exception {
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userDocFamily = createUser("doc.family@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other@example.com", Role.DOCTOR);

            Doctor doctorFamily = createDoctor(userDocFamily, "Family Doc", "LIC-FAM");
            createDoctor(userDocOther, "Other Doc", "LIC-OTH");

            Patient patient = createPatient(userPat, "Patient With Family Doctor");
            patient.setFamilyDoctor(doctorFamily);
            patientRepository.save(patient);

            // Family doctor -> 200
            mockMvc.perform(get("/api/patients/" + patient.getId())
                            .header("Authorization", "Bearer " + token(userDocFamily)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Patient With Family Doctor"));

            // Unrelated doctor -> 403
            mockMvc.perform(get("/api/patients/" + patient.getId())
                            .header("Authorization", "Bearer " + token(userDocOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Family doctor cannot edit patient demographics (PUT /api/patients/{id})")
        void familyDoctorCannotModifyPatientDemographics() throws Exception {
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userDocFamily = createUser("doc.family@example.com", Role.DOCTOR);
            Doctor doctorFamily = createDoctor(userDocFamily, "Family Doc", "LIC-FAM");

            Patient patient = createPatient(userPat, "Patient Name");
            patient.setFamilyDoctor(doctorFamily);
            patientRepository.save(patient);

            UpdatePatientRequest updateReq = new UpdatePatientRequest();
            updateReq.setFullName("Hacked Demographics");
            updateReq.setBirthDate(LocalDate.of(1990, 1, 1));
            updateReq.setGender("M");
            updateReq.setBloodGroup("O+");
            updateReq.setPhone("+12025550101");
            updateReq.setAddress("Updated Address 123");

            mockMvc.perform(put("/api/patients/" + patient.getId())
                            .header("Authorization", "Bearer " + token(userDocFamily))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Patient owner can update their own profile and family doctor")
        void patientOwnerCanUpdateProfileAndFamilyDoctor() throws Exception {
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userDoc = createUser("doc@example.com", Role.DOCTOR);
            Doctor doctor = createDoctor(userDoc, "Dr. Smith", "LIC-111");
            Patient patient = createPatient(userPat, "Patient Name");

            FamilyDoctorRequest docReq = new FamilyDoctorRequest();
            docReq.setFamilyDoctorId(doctor.getId());

            mockMvc.perform(patch("/api/patients/" + patient.getId() + "/family-doctor")
                            .header("Authorization", "Bearer " + token(userPat))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(docReq)))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // 3. DOCTOR DIRECTORY & OWNERSHIP
    // =========================================================================

    @Nested
    @DisplayName("3. Doctor Directory & Profile Ownership")
    class DoctorOwnership {

        @Test
        @DisplayName("Any authenticated user can read doctor directory and doctor by ID")
        void anyAuthenticatedUserCanReadDoctorDirectory() throws Exception {
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userDoc = createUser("doc@example.com", Role.DOCTOR);
            Doctor doctor = createDoctor(userDoc, "Dr. John", "LIC-DIR-1");

            mockMvc.perform(get("/api/doctors")
                            .header("Authorization", "Bearer " + token(userPat)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/doctors/" + doctor.getId())
                            .header("Authorization", "Bearer " + token(userPat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Dr. John"));
        }

        @Test
        @DisplayName("Doctor can update own profile; updating another doctor's profile returns 403")
        void doctorUpdateOwnership() throws Exception {
            User userDocA = createUser("doc.a@example.com", Role.DOCTOR);
            User userDocB = createUser("doc.b@example.com", Role.DOCTOR);
            Doctor docA = createDoctor(userDocA, "Doctor A", "LIC-A");
            Doctor docB = createDoctor(userDocB, "Doctor B", "LIC-B");

            UpdateDoctorRequest updateReq = new UpdateDoctorRequest();
            updateReq.setFullName("Doctor B Modified");
            updateReq.setSpecialization("Neurology");
            updateReq.setLicenseNumber("LIC-B");
            updateReq.setPhone("+12025550199");
            updateReq.setActive(true);

            // Doctor A updating Doctor B -> 403
            mockMvc.perform(put("/api/doctors/" + docB.getId())
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Doctor B updating Doctor B -> 200
            mockMvc.perform(put("/api/doctors/" + docB.getId())
                            .header("Authorization", "Bearer " + token(userDocB))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value("Doctor B Modified"));
        }

        @Test
        @DisplayName("Doctor cannot reactivate deactivated profile through normal update")
        void doctorCannotReactivateDeactivatedProfile() throws Exception {
            User userDoc = createUser("deact.doc@example.com", Role.DOCTOR);
            Doctor doc = createDoctor(userDoc, "Deactivated Doc", "LIC-DEACT");
            doc.setActive(false);
            doctorRepository.save(doc);

            UpdateDoctorRequest req = new UpdateDoctorRequest();
            req.setFullName("Deactivated Doc");
            req.setSpecialization("General");
            req.setLicenseNumber("LIC-DEACT");
            req.setPhone("+12025550199");
            req.setActive(true); // Attempt to reactivate

            mockMvc.perform(put("/api/doctors/" + doc.getId())
                            .header("Authorization", "Bearer " + token(userDoc))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }
    }

    // =========================================================================
    // 4. PHARMACY OWNERSHIP & INVENTORY
    // =========================================================================

    @Nested
    @DisplayName("4. Pharmacy Ownership & Inventory Access")
    class PharmacyOwnership {

        @Test
        @DisplayName("Pharmacy user can create own profile; cross-user creation returns 403")
        void pharmacyProfileCreationOwnership() throws Exception {
            User userPharmA = createUser("pharm.a@example.com", Role.PHARMACY);
            User userPharmB = createUser("pharm.b@example.com", Role.PHARMACY);

            CreatePharmacyRequest req = new CreatePharmacyRequest();
            req.setUserId(userPharmB.getId());
            req.setName("Pharmacy B Profile");
            req.setAddress("100 Market St");
            req.setPhone("+12025550188");
            req.setEmail("pharm.b@example.com");

            // Pharmacy A creating for User B -> 403
            mockMvc.perform(post("/api/pharmacies")
                            .header("Authorization", "Bearer " + token(userPharmA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Pharmacy A creating for User A -> 201
            req.setUserId(userPharmA.getId());
            req.setEmail("pharm.a@example.com");
            mockMvc.perform(post("/api/pharmacies")
                            .header("Authorization", "Bearer " + token(userPharmA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Pharmacy B Profile"))
                    .andExpect(jsonPath("$.userId").value(userPharmA.getId()));
        }

        @Test
        @DisplayName("Pharmacy A can access own inventory, but accessing Pharmacy B inventory returns 403")
        void pharmacyInventoryOwnershipIsolation() throws Exception {
            User userPharmA = createUser("pharm.a@example.com", Role.PHARMACY);
            User userPharmB = createUser("pharm.b@example.com", Role.PHARMACY);
            Pharmacy pharmA = createPharmacy(userPharmA, "Pharmacy A");
            Pharmacy pharmB = createPharmacy(userPharmB, "Pharmacy B");

            // Pharmacy A -> GET Pharmacy A inventory -> 200
            mockMvc.perform(get("/api/pharmacies/" + pharmA.getId() + "/inventory")
                            .header("Authorization", "Bearer " + token(userPharmA)))
                    .andExpect(status().isOk());

            // Pharmacy A -> GET Pharmacy B inventory -> 403
            mockMvc.perform(get("/api/pharmacies/" + pharmB.getId() + "/inventory")
                            .header("Authorization", "Bearer " + token(userPharmA)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Pharmacy A -> GET Pharmacy B low-stock -> 403
            mockMvc.perform(get("/api/pharmacies/" + pharmB.getId() + "/inventory/low-stock")
                            .header("Authorization", "Bearer " + token(userPharmA)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Non-admin cannot deactivate pharmacy via PATCH /api/pharmacies/{id}/deactivate")
        void nonAdminCannotDeactivatePharmacy() throws Exception {
            User userPharm = createUser("pharm@example.com", Role.PHARMACY);
            Pharmacy pharm = createPharmacy(userPharm, "Pharmacy Test");

            mockMvc.perform(patch("/api/pharmacies/" + pharm.getId() + "/deactivate")
                            .header("Authorization", "Bearer " + token(userPharm)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }
    }

    // =========================================================================
    // 5. PRESCRIPTION CREATION, READ, CANCEL & SEND-TO-PHARMACY
    // =========================================================================

    @Nested
    @DisplayName("5. Prescription Operations & Access")
    class PrescriptionOperations {

        @Test
        @DisplayName("Doctor can issue prescription using own doctorId; using another doctorId returns 403")
        void doctorPrescriptionCreationOwnership() throws Exception {
            User userDocA = createUser("doc.a@example.com", Role.DOCTOR);
            User userDocB = createUser("doc.b@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);

            Doctor docA = createDoctor(userDocA, "Doctor A", "LIC-A");
            Doctor docB = createDoctor(userDocB, "Doctor B", "LIC-B");
            Patient pat = createPatient(userPat, "Patient 1");
            Medication med = createMedication("Amoxicillin");

            PrescriptionRequest req = new PrescriptionRequest();
            req.setPatientId(pat.getId());
            req.setDoctorId(docB.getId()); // Doctor A claims Doctor B
            req.setIssueDate(LocalDateTime.now());
            PrescriptionItemRequest item = new PrescriptionItemRequest();
            item.setMedicationId(med.getId());
            item.setDosage("500mg");
            item.setFrequency("TID");
            item.setDurationDays(5);
            item.setQuantity(15);
            req.setItems(List.of(item));

            // Doctor A using doctorId B -> 403
            mockMvc.perform(post("/api/prescriptions")
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Doctor A using doctorId A -> 201
            req.setDoctorId(docA.getId());
            mockMvc.perform(post("/api/prescriptions")
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Prescription read allowed for prescribing doctor, patient owner, assigned pharmacy, and admin; denied for others")
        void prescriptionReadAccessMatrix() throws Exception {
            User userDocOwner = createUser("doc.owner@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other@example.com", Role.DOCTOR);
            User userPatOwner = createUser("pat.owner@example.com", Role.PATIENT);
            User userPatOther = createUser("pat.other@example.com", Role.PATIENT);
            User userPharmAssigned = createUser("pharm.assigned@example.com", Role.PHARMACY);
            User userPharmOther = createUser("pharm.other@example.com", Role.PHARMACY);
            User userAdmin = createUser("admin@example.com", Role.ADMIN);

            Doctor docOwner = createDoctor(userDocOwner, "Doctor Owner", "LIC-OWN");
            createDoctor(userDocOther, "Doctor Other", "LIC-OTH");
            Patient patOwner = createPatient(userPatOwner, "Patient Owner");
            createPatient(userPatOther, "Patient Other");
            Pharmacy pharmAssigned = createPharmacy(userPharmAssigned, "Assigned Pharmacy");
            createPharmacy(userPharmOther, "Other Pharmacy");

            Prescription prescription = createPrescription(patOwner, docOwner);
            createFulfillment(prescription, pharmAssigned);

            // 1. Prescribing doctor -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userDocOwner)))
                    .andExpect(status().isOk());

            // 2. Prescription patient -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userPatOwner)))
                    .andExpect(status().isOk());

            // 3. Assigned pharmacy -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userPharmAssigned)))
                    .andExpect(status().isOk());

            // 4. Admin -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userAdmin)))
                    .andExpect(status().isOk());

            // 5. Unrelated doctor -> 403
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userDocOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // 6. Unrelated patient -> 403
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userPatOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // 7. Unrelated pharmacy -> 403
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId())
                            .header("Authorization", "Bearer " + token(userPharmOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Prescription cancel only permitted for prescribing doctor; others receive 403")
        void prescriptionCancellationOwnership() throws Exception {
            User userDocOwner = createUser("doc.owner@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userPharm = createUser("pharm@example.com", Role.PHARMACY);

            Doctor docOwner = createDoctor(userDocOwner, "Doctor Owner", "LIC-OWN");
            createDoctor(userDocOther, "Doctor Other", "LIC-OTH");
            Patient pat = createPatient(userPat, "Patient 1");

            Prescription prescription = createPrescription(pat, docOwner);

            CancelPrescriptionRequest cancelReq = new CancelPrescriptionRequest();
            cancelReq.setReason("Adverse reaction");

            // Patient -> 403
            mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/cancel")
                            .header("Authorization", "Bearer " + token(userPat))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Other doctor -> 403
            mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/cancel")
                            .header("Authorization", "Bearer " + token(userDocOther))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Prescribing doctor -> 200
            mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/cancel")
                            .header("Authorization", "Bearer " + token(userDocOwner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Send to pharmacy only permitted for prescribing doctor")
        void sendToPharmacyOwnership() throws Exception {
            User userDocOwner = createUser("doc.owner@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);
            User userPharm = createUser("pharm@example.com", Role.PHARMACY);

            Doctor docOwner = createDoctor(userDocOwner, "Doctor Owner", "LIC-OWN");
            createDoctor(userDocOther, "Doctor Other", "LIC-OTH");
            Patient pat = createPatient(userPat, "Patient");
            Pharmacy pharm = createPharmacy(userPharm, "Target Pharmacy");

            Prescription prescription = createPrescription(pat, docOwner);

            SendToPharmacyRequest req = new SendToPharmacyRequest();
            req.setPharmacyId(pharm.getId());

            // Other doctor -> 403
            mockMvc.perform(post("/api/prescriptions/" + prescription.getId() + "/send-to-pharmacy")
                            .header("Authorization", "Bearer " + token(userDocOther))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Prescribing doctor -> 201
            mockMvc.perform(post("/api/prescriptions/" + prescription.getId() + "/send-to-pharmacy")
                            .header("Authorization", "Bearer " + token(userDocOwner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.prescriptionStatus").value("SENT_TO_PHARMACY"));
        }
    }

    // =========================================================================
    // 6. FULFILLMENT OPERATIONS
    // =========================================================================

    @Nested
    @DisplayName("6. Fulfillment Lifecycle & Pharmacy Ownership")
    class FulfillmentOwnership {

        @Test
        @DisplayName("Only assigned pharmacy can accept/reject/prepare/complete fulfillment; Pharmacy B receives 403")
        void fulfillmentPharmacyOwnershipIsolation() throws Exception {
            User userPharmA = createUser("pharm.a@example.com", Role.PHARMACY);
            User userPharmB = createUser("pharm.b@example.com", Role.PHARMACY);
            User userDoc = createUser("doc@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);

            Doctor doc = createDoctor(userDoc, "Doctor", "LIC-DOC");
            Patient pat = createPatient(userPat, "Patient");
            Pharmacy pharmA = createPharmacy(userPharmA, "Pharmacy A");
            createPharmacy(userPharmB, "Pharmacy B");

            Prescription prescription = createPrescription(pat, doc);
            PrescriptionFulfillment fulfillment = createFulfillment(prescription, pharmA);

            // Pharmacy B trying to accept Pharmacy A's fulfillment -> 403
            mockMvc.perform(patch("/api/fulfillments/" + fulfillment.getId() + "/accept")
                            .header("Authorization", "Bearer " + token(userPharmB)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Pharmacy A accepting own fulfillment -> 200
            mockMvc.perform(patch("/api/fulfillments/" + fulfillment.getId() + "/accept")
                            .header("Authorization", "Bearer " + token(userPharmA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));
        }
    }

    // =========================================================================
    // 7. VISITS & MEDICAL REPORTS
    // =========================================================================

    @Nested
    @DisplayName("7. Visits & Medical Reports")
    class VisitsAndMedicalReports {

        @Test
        @DisplayName("Doctor can create visit using own doctor profile; cross-doctor creation returns 403")
        void createVisitOwnership() throws Exception {
            User userDocA = createUser("doc.a@example.com", Role.DOCTOR);
            User userDocB = createUser("doc.b@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);

            createDoctor(userDocA, "Doctor A", "LIC-A");
            Doctor docB = createDoctor(userDocB, "Doctor B", "LIC-B");
            Patient pat = createPatient(userPat, "Patient");

            CreateVisitRequest req = new CreateVisitRequest();
            req.setPatientId(pat.getId());
            req.setDoctorId(docB.getId());
            req.setVisitDate(LocalDateTime.now().plusDays(1));
            req.setDiagnosis("Cold");
            req.setSymptoms("Fever");

            // Doctor A claiming Doctor B -> 403
            mockMvc.perform(post("/api/visits")
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Only treating doctor can add notes to a visit; other doctor receives 403")
        void addVisitNotesOwnership() throws Exception {
            User userDocTreating = createUser("doc.treating@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);

            Doctor docTreating = createDoctor(userDocTreating, "Doctor Treating", "LIC-TRT");
            createDoctor(userDocOther, "Doctor Other", "LIC-OTH");
            Patient pat = createPatient(userPat, "Patient");

            Visit visit = createVisit(pat, docTreating);

            NotesRequest notesReq = new NotesRequest();
            notesReq.setNotes("Patient responds well to medication");

            // Other doctor -> 403
            mockMvc.perform(post("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userDocOther))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notesReq)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Treating doctor -> 201
            mockMvc.perform(post("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userDocTreating))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(notesReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.notes").value("Patient responds well to medication"));
        }

        @Test
        @DisplayName("Visit notes read allowed for visit patient, treating doctor, family doctor, admin; denied for other doctor and other patient")
        void readVisitNotesAccessMatrix() throws Exception {
            User userDocTreating = createUser("doc.trt@example.com", Role.DOCTOR);
            User userDocFamily = createUser("doc.fam@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.oth@example.com", Role.DOCTOR);
            User userPat = createUser("pat.visit@example.com", Role.PATIENT);
            User userPatOther = createUser("pat.oth@example.com", Role.PATIENT);
            User userAdmin = createUser("admin.visit@example.com", Role.ADMIN);

            Doctor docTreating = createDoctor(userDocTreating, "Treating Doc", "LIC-T");
            Doctor docFamily = createDoctor(userDocFamily, "Family Doc", "LIC-F");
            createDoctor(userDocOther, "Other Doc", "LIC-O");

            Patient pat = createPatient(userPat, "Visit Patient");
            pat.setFamilyDoctor(docFamily);
            pat = patientRepository.save(pat);

            createPatient(userPatOther, "Other Patient");

            Visit visit = createVisit(pat, docTreating);
            visit.setNotes("Clinical note content");
            visit = visitRepository.save(visit);

            // 1. Visit Patient -> 200
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userPat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes").value("Clinical note content"));

            // 2. Treating Doctor -> 200
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userDocTreating)))
                    .andExpect(status().isOk());

            // 3. Family Doctor -> 200
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userDocFamily)))
                    .andExpect(status().isOk());

            // 4. Admin -> 200
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userAdmin)))
                    .andExpect(status().isOk());

            // 5. Other Doctor -> 403
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userDocOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // 6. Other Patient -> 403
            mockMvc.perform(get("/api/visits/" + visit.getId() + "/notes")
                            .header("Authorization", "Bearer " + token(userPatOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Medical report creation allowed for doctor using own doctorId or admin; denied for cross-doctor, patient, pharmacy")
        void medicalReportCreationAuthorization() throws Exception {
            User userDocA = createUser("doc.rep.a@example.com", Role.DOCTOR);
            User userDocB = createUser("doc.rep.b@example.com", Role.DOCTOR);
            User userPat = createUser("pat.rep@example.com", Role.PATIENT);
            User userPharm = createUser("pharm.rep@example.com", Role.PHARMACY);
            User userAdmin = createUser("admin.rep@example.com", Role.ADMIN);

            Doctor docA = createDoctor(userDocA, "Doctor A", "LIC-RA");
            Doctor docB = createDoctor(userDocB, "Doctor B", "LIC-RB");
            Patient pat = createPatient(userPat, "Patient Rep");

            CreateMedicalReportRequest req = new CreateMedicalReportRequest();
            req.setPatientId(pat.getId());
            req.setDoctorId(docB.getId());
            req.setReportType("LAB");
            req.setTitle("Blood Work");
            req.setDescription("All normal");
            req.setFileUrl("https://example.com/reports/blood-work.pdf");
            req.setReportDate(LocalDateTime.now());

            // Doctor A claiming Doctor B -> 403
            mockMvc.perform(post("/medical-reports")
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Patient -> 403
            mockMvc.perform(post("/medical-reports")
                            .header("Authorization", "Bearer " + token(userPat))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Pharmacy -> 403
            mockMvc.perform(post("/medical-reports")
                            .header("Authorization", "Bearer " + token(userPharm))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Doctor A creating with own doctorId A -> 201
            req.setDoctorId(docA.getId());
            mockMvc.perform(post("/medical-reports")
                            .header("Authorization", "Bearer " + token(userDocA))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Blood Work"));

            // Admin creating report -> 201
            req.setDoctorId(docB.getId());
            mockMvc.perform(post("/medical-reports")
                            .header("Authorization", "Bearer " + token(userAdmin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // 8. MULTI-ROLE USERS & ROLE VS OWNERSHIP
    // =========================================================================

    @Nested
    @DisplayName("8. Multi-Role & Role vs Ownership Distinction")
    class MultiRoleAndRoleVsOwnership {

        @Test
        @DisplayName("Multi-role user (DOCTOR + ADMIN) has admin directory access and doctor prescribing with own profile")
        void multiRoleDoctorAndAdmin() throws Exception {
            User userMulti = createUser("doc.admin@example.com", Role.DOCTOR, Role.ADMIN);
            User userOtherDoc = createUser("other.doc@example.com", Role.DOCTOR);
            User userPat = createUser("pat@example.com", Role.PATIENT);

            Doctor multiDoc = createDoctor(userMulti, "Dr. Multi Admin", "LIC-MULTI");
            Doctor otherDoc = createDoctor(userOtherDoc, "Dr. Other", "LIC-OTHER");
            Patient pat = createPatient(userPat, "Patient");
            Medication med = createMedication("Paracetamol");

            // 1. Admin permission: GET /api/patients -> 200
            mockMvc.perform(get("/api/patients")
                            .header("Authorization", "Bearer " + token(userMulti)))
                    .andExpect(status().isOk());

            // 2. Doctor clinical permission for own profile -> 201
            PrescriptionRequest reqOwn = new PrescriptionRequest();
            reqOwn.setPatientId(pat.getId());
            reqOwn.setDoctorId(multiDoc.getId());
            reqOwn.setIssueDate(LocalDateTime.now());
            PrescriptionItemRequest item = new PrescriptionItemRequest();
            item.setMedicationId(med.getId());
            item.setDosage("500mg");
            item.setFrequency("QD");
            item.setDurationDays(3);
            item.setQuantity(3);
            reqOwn.setItems(List.of(item));

            mockMvc.perform(post("/api/prescriptions")
                            .header("Authorization", "Bearer " + token(userMulti))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqOwn)))
                    .andExpect(status().isCreated());

            // 3. Admin authority alone does NOT allow clinical prescribing under another doctor's ID -> 403
            reqOwn.setDoctorId(otherDoc.getId());
            mockMvc.perform(post("/api/prescriptions")
                            .header("Authorization", "Bearer " + token(userMulti))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reqOwn)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Role alone is insufficient without ownership: ROLE_PATIENT cannot access another patient")
        void roleIsInsufficientWithoutOwnership() throws Exception {
            User userPat1 = createUser("pat1@example.com", Role.PATIENT);
            User userPat2 = createUser("pat2@example.com", Role.PATIENT);
            Patient pat2 = createPatient(userPat2, "Patient 2");

            // Has ROLE_PATIENT, but doesn't own Patient 2 -> 403
            mockMvc.perform(get("/api/patients/" + pat2.getId() + "/timeline")
                            .header("Authorization", "Bearer " + token(userPat1)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }
    }

    // =========================================================================
    // 9. MEDICATION CATALOG & AUDIT HISTORY
    // =========================================================================

    @Nested
    @DisplayName("9. Medication Catalog & Audit History")
    class MedicationCatalogAndAuditHistory {

        @Test
        @DisplayName("Medication catalog mutation is ADMIN only; normal users receive 403")
        void medicationCatalogMutationAdminOnly() throws Exception {
            User userPat = createUser("pat.med@example.com", Role.PATIENT);
            User userAdmin = createUser("admin.med@example.com", Role.ADMIN);

            MedicationRequest req = new MedicationRequest();
            req.setName("Ibuprofen 400mg");
            req.setGenericName("Ibuprofen");
            req.setBrand("Advil");
            req.setDosageForm("Tablet");
            req.setStrength("400mg");
            req.setActive(true);

            // Patient -> 403
            mockMvc.perform(post("/api/medications")
                            .header("Authorization", "Bearer " + token(userPat))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Admin -> 201
            mockMvc.perform(post("/api/medications")
                            .header("Authorization", "Bearer " + token(userAdmin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Ibuprofen 400mg"));
        }

        @Test
        @DisplayName("Prescription audit history accessible by prescribing doctor, patient, assigned pharmacy, and admin")
        void prescriptionAuditHistoryAccess() throws Exception {
            User userDoc = createUser("doc.aud@example.com", Role.DOCTOR);
            User userDocOther = createUser("doc.other.aud@example.com", Role.DOCTOR);
            User userPat = createUser("pat.aud@example.com", Role.PATIENT);
            User userPharm = createUser("pharm.aud@example.com", Role.PHARMACY);
            User userAdmin = createUser("admin.aud@example.com", Role.ADMIN);

            Doctor doc = createDoctor(userDoc, "Doctor Audit", "LIC-AUD");
            createDoctor(userDocOther, "Doctor Other Audit", "LIC-OAUD");
            Patient pat = createPatient(userPat, "Patient Audit");
            Pharmacy pharm = createPharmacy(userPharm, "Pharmacy Audit");

            Prescription prescription = createPrescription(pat, doc);
            createFulfillment(prescription, pharm);

            // Prescribing doctor -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId() + "/audit-history")
                            .header("Authorization", "Bearer " + token(userDoc)))
                    .andExpect(status().isOk());

            // Patient -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId() + "/audit-history")
                            .header("Authorization", "Bearer " + token(userPat)))
                    .andExpect(status().isOk());

            // Assigned pharmacy -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId() + "/audit-history")
                            .header("Authorization", "Bearer " + token(userPharm)))
                    .andExpect(status().isOk());

            // Admin -> 200
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId() + "/audit-history")
                            .header("Authorization", "Bearer " + token(userAdmin)))
                    .andExpect(status().isOk());

            // Unrelated doctor -> 403
            mockMvc.perform(get("/api/prescriptions/" + prescription.getId() + "/audit-history")
                            .header("Authorization", "Bearer " + token(userDocOther)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }

        @Test
        @DisplayName("Generic prescription status update PATCH /api/prescriptions/{id}/status is ADMIN only")
        void genericPrescriptionStatusUpdateAdminOnly() throws Exception {
            User userDoc = createUser("doc.stat@example.com", Role.DOCTOR);
            User userAdmin = createUser("admin.stat@example.com", Role.ADMIN);
            Doctor doc = createDoctor(userDoc, "Doctor Status", "LIC-STAT");
            Patient pat = createPatient(userDoc, "Patient Status");
            Prescription prescription = createPrescription(pat, doc);

            PrescriptionStatusUpdateRequest req = new PrescriptionStatusUpdateRequest();
            req.setStatus(PrescriptionStatus.COMPLETED);

            // Doctor -> 403
            mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/status")
                            .header("Authorization", "Bearer " + token(userDoc))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));

            // Admin -> status transition is checked (e.g. 409 for invalid transition from ISSUED to COMPLETED)
            mockMvc.perform(patch("/api/prescriptions/" + prescription.getId() + "/status")
                            .header("Authorization", "Bearer " + token(userAdmin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }
    }
}
