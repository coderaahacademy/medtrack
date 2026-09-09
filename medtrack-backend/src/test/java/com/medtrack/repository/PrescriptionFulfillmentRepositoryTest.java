package com.medtrack.repository;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T46: Repository slice for the pharmacy fulfillment queue.
 *
 * Verifies the JPQL and derived queries themselves - tenant scoping, status filtering,
 * paging, ordering and the detail fetch graph - without the web or security layer.
 */
@DataJpaTest
class PrescriptionFulfillmentRepositoryTest {

    @Autowired private PrescriptionFulfillmentRepository fulfillmentRepository;
    @Autowired private PrescriptionItemRepository itemRepository;
    @Autowired private PrescriptionRepository prescriptionRepository;
    @Autowired private MedicationRepository medicationRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PharmacyRepository pharmacyRepository;
    @Autowired private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Pharmacy pharmacyA;
    private Pharmacy pharmacyB;

    private PrescriptionFulfillment oldestA;
    private PrescriptionFulfillment middleA;
    private PrescriptionFulfillment newestA;
    private PrescriptionFulfillment fulfillmentB;

    @BeforeEach
    void setUp() {
        Patient patient = createPatient(createUser("patient@repo.test", Role.PATIENT), "Anna Muster");
        Doctor doctor = createDoctor(createUser("doctor@repo.test", Role.DOCTOR), "Dr. Bernd Arzt", "LIC-2001");
        Medication medication = createMedication("Amoxicillin");

        pharmacyA = createPharmacy(createUser("pharmacy-a@repo.test", Role.PHARMACY), "Apotheke A");
        pharmacyB = createPharmacy(createUser("pharmacy-b@repo.test", Role.PHARMACY), "Apotheke B");

        Prescription p1 = createPrescription(patient, doctor);
        createItem(p1, medication, 10, 4);
        createItem(p1, medication, 5, 0);

        Prescription p2 = createPrescription(patient, doctor);
        createItem(p2, medication, 6, 0);

        Prescription p3 = createPrescription(patient, doctor);
        createItem(p3, medication, 8, 0);

        Prescription p4 = createPrescription(patient, doctor);
        createItem(p4, medication, 3, 0);

        // createdAt is set by @PrePersist, so it is overwritten afterwards to get a
        // deterministic order instead of three rows in the same millisecond.
        oldestA = createFulfillment(p1, pharmacyA, FulfillmentStatus.PENDING);
        middleA = createFulfillment(p2, pharmacyA, FulfillmentStatus.ACCEPTED);
        newestA = createFulfillment(p3, pharmacyA, FulfillmentStatus.PENDING);
        fulfillmentB = createFulfillment(p4, pharmacyB, FulfillmentStatus.PENDING);

        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);
        setCreatedAt(oldestA, base);
        setCreatedAt(middleA, base.plusHours(1));
        setCreatedAt(newestA, base.plusHours(2));
        setCreatedAt(fulfillmentB, base.plusHours(3));

        entityManager.flush();
        entityManager.clear();
    }

    // =========================================================================
    // TENANT SCOPING
    // =========================================================================

    @Test
    @DisplayName("Queue contains only the fulfillments of the requested pharmacy")
    void queueIsScopedToOnePharmacy() {
        Page<PrescriptionFulfillment> page =
                fulfillmentRepository.findByPharmacyId(pharmacyA.getId(), PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(f -> f.getPharmacy().getId())
                .containsOnly(pharmacyA.getId());
        assertThat(page.getContent())
                .extracting(PrescriptionFulfillment::getId)
                .doesNotContain(fulfillmentB.getId());
    }

    @Test
    @DisplayName("Queue of a pharmacy without fulfillments is empty, not a fallback to all rows")
    void unknownPharmacyYieldsEmptyPage() {
        Page<PrescriptionFulfillment> page =
                fulfillmentRepository.findByPharmacyId(-1L, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    // =========================================================================
    // STATUS FILTER
    // =========================================================================

    @Test
    @DisplayName("Status filter narrows the queue and stays inside the pharmacy")
    void statusFilterNarrowsQueue() {
        Page<PrescriptionFulfillment> pending = fulfillmentRepository
                .findByPharmacyIdAndStatus(pharmacyA.getId(), FulfillmentStatus.PENDING, PageRequest.of(0, 20));

        assertThat(pending.getTotalElements()).isEqualTo(2);
        assertThat(pending.getContent())
                .extracting(PrescriptionFulfillment::getId)
                .containsExactlyInAnyOrder(oldestA.getId(), newestA.getId());
    }

    @Test
    @DisplayName("Status filter does not reach into another pharmacy's queue")
    void statusFilterDoesNotCrossPharmacies() {
        Page<PrescriptionFulfillment> pending = fulfillmentRepository
                .findByPharmacyIdAndStatus(pharmacyA.getId(), FulfillmentStatus.PENDING, PageRequest.of(0, 20));

        assertThat(pending.getContent())
                .extracting(PrescriptionFulfillment::getId)
                .doesNotContain(fulfillmentB.getId());
    }

    @Test
    @DisplayName("Status with no matching row returns an empty page")
    void statusWithoutMatchesReturnsEmptyPage() {
        Page<PrescriptionFulfillment> completed = fulfillmentRepository
                .findByPharmacyIdAndStatus(pharmacyA.getId(), FulfillmentStatus.COMPLETED, PageRequest.of(0, 20));

        assertThat(completed.getTotalElements()).isZero();
    }

    // =========================================================================
    // PAGING AND ORDERING
    // =========================================================================

    @Test
    @DisplayName("Paging reports the full total while returning one page")
    void pagingKeepsTotalCount() {
        Page<PrescriptionFulfillment> firstPage = fulfillmentRepository
                .findByPharmacyId(pharmacyA.getId(), PageRequest.of(0, 2));

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(2);

        Page<PrescriptionFulfillment> secondPage = fulfillmentRepository
                .findByPharmacyId(pharmacyA.getId(), PageRequest.of(1, 2));

        assertThat(secondPage.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Newest first and oldest first return opposite orders")
    void orderingCanBeFlipped() {
        List<Long> newestFirst = fulfillmentRepository
                .findByPharmacyId(pharmacyA.getId(),
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream().map(PrescriptionFulfillment::getId).toList();

        List<Long> oldestFirst = fulfillmentRepository
                .findByPharmacyId(pharmacyA.getId(),
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "createdAt")))
                .getContent().stream().map(PrescriptionFulfillment::getId).toList();

        assertThat(newestFirst).containsExactly(newestA.getId(), middleA.getId(), oldestA.getId());
        assertThat(oldestFirst).containsExactly(oldestA.getId(), middleA.getId(), newestA.getId());
    }

    // =========================================================================
    // DETAIL FETCH GRAPH
    // =========================================================================

    @Test
    @DisplayName("Detail query loads pharmacy, prescription, patient, items and medication in one go")
    void detailLoadsFullGraph() {
        entityManager.clear();

        Optional<PrescriptionFulfillment> found = fulfillmentRepository.findDetailById(oldestA.getId());

        assertThat(found).isPresent();
        PrescriptionFulfillment fulfillment = found.get();

        // Everything below is read after detaching, so a missing JOIN FETCH would fail here.
        entityManager.detach(fulfillment);

        assertThat(fulfillment.getPharmacy().getName()).isEqualTo("Apotheke A");
        assertThat(fulfillment.getPrescription().getPatient().getFullName()).isEqualTo("Anna Muster");
        assertThat(fulfillment.getPrescription().getItems()).hasSize(2);
        assertThat(fulfillment.getPrescription().getItems())
                .allSatisfy(item -> assertThat(item.getMedication().getName()).isEqualTo("Amoxicillin"));
    }

    @Test
    @DisplayName("Detail query returns empty for an unknown id instead of throwing")
    void detailReturnsEmptyForUnknownId() {
        assertThat(fulfillmentRepository.findDetailById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("Collection fetch join does not duplicate the fulfillment row")
    void detailDoesNotDuplicateRootRow() {
        // oldestA has two prescription items; without de-duplication the fetch join
        // would produce two root rows and Optional would blow up.
        assertThat(fulfillmentRepository.findDetailById(oldestA.getId())).isPresent();
    }

    // =========================================================================
    // ITEM COUNTS
    // =========================================================================

    @Test
    @DisplayName("Item counts come back grouped per prescription in one query")
    void itemCountsAreGroupedPerPrescription() {
        Long prescriptionWithTwoItems = oldestA.getPrescription().getId();
        Long prescriptionWithOneItem = middleA.getPrescription().getId();

        List<Object[]> rows = itemRepository.countItemsByPrescriptionIds(
                List.of(prescriptionWithTwoItems, prescriptionWithOneItem));

        assertThat(rows).hasSize(2);
        assertThat(rows)
                .anySatisfy(row -> {
                    assertThat(row[0]).isEqualTo(prescriptionWithTwoItems);
                    assertThat(((Number) row[1]).intValue()).isEqualTo(2);
                })
                .anySatisfy(row -> {
                    assertThat(row[0]).isEqualTo(prescriptionWithOneItem);
                    assertThat(((Number) row[1]).intValue()).isEqualTo(1);
                });
    }

    // =========================================================================
    // FIXTURES
    // =========================================================================

    private void setCreatedAt(PrescriptionFulfillment fulfillment, LocalDateTime createdAt) {
        entityManager.createQuery(
                        "UPDATE PrescriptionFulfillment f SET f.createdAt = :createdAt WHERE f.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", fulfillment.getId())
                .executeUpdate();
    }

    private User createUser(String email, Role... roles) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);
        for (Role role : roles) {
            user.addRole(role);
        }
        return userRepository.save(user);
    }

    private Pharmacy createPharmacy(User user, String name) {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setUser(user);
        pharmacy.setName(name);
        pharmacy.setAddress("456 Pharmacy Ave");
        pharmacy.setPhone("+12025550103");
        pharmacy.setEmail(user.getEmail());
        pharmacy.setActive(true);
        return pharmacyRepository.save(pharmacy);
    }

    private Patient createPatient(User user, String name) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFullName(name);
        patient.setBirthDate(LocalDate.of(1990, 5, 17));
        patient.setGender("F");
        patient.setBloodGroup("O+");
        patient.setAllergies("Penicillin");
        patient.setPhone("+12025550101");
        patient.setAddress("123 Main St");
        return patientRepository.save(patient);
    }

    private Doctor createDoctor(User user, String name, String license) {
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setFullName(name);
        doctor.setLicenseNumber(license);
        doctor.setSpecialization("General");
        doctor.setPhone("+12025550102");
        doctor.setActive(true);
        return doctorRepository.save(doctor);
    }

    private Medication createMedication(String name) {
        Medication medication = new Medication();
        medication.setName(name);
        medication.setBrand("BrandX");
        medication.setGenericName("GenericX");
        medication.setStrength("500mg");
        medication.setDosageForm("Tablet");
        medication.setActive(true);
        return medicationRepository.save(medication);
    }

    private Prescription createPrescription(Patient patient, Doctor doctor) {
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setStatus(PrescriptionStatus.SENT_TO_PHARMACY);
        prescription.setIssueDate(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    private PrescriptionItem createItem(Prescription prescription, Medication medication,
                                        int quantity, int dispensed) {
        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(prescription);
        item.setMedication(medication);
        item.setDosage("500mg");
        item.setFrequency("2x daily");
        item.setDurationDays(5);
        item.setQuantity(quantity);
        item.setDispensedQuantity(dispensed);
        return itemRepository.save(item);
    }

    private PrescriptionFulfillment createFulfillment(Prescription prescription, Pharmacy pharmacy,
                                                      FulfillmentStatus status) {
        PrescriptionFulfillment fulfillment = new PrescriptionFulfillment();
        fulfillment.setPrescription(prescription);
        fulfillment.setPharmacy(pharmacy);
        fulfillment.setStatus(status);
        fulfillment.setRequestedAt(LocalDateTime.now());
        return fulfillmentRepository.save(fulfillment);
    }
}
