package com.medtrack.service;

import com.medtrack.entity.PrescriptionAudit;
import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.repository.PrescriptionAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class FulfillmentRollbackIntegrationTest {

    private static final long PATIENT_USER_ID = 11001L;
    private static final long DOCTOR_USER_ID = 11002L;
    private static final long PHARMACY_USER_ID = 11003L;

    private static final long PATIENT_ID = 11101L;
    private static final long DOCTOR_ID = 11102L;
    private static final long PHARMACY_ID = 11103L;

    private static final long PRESCRIPTION_ID = 11201L;
    private static final long FULFILLMENT_ID = 11202L;
    private static final long MEDICATION_ID = 11203L;
    private static final long PRESCRIPTION_ITEM_ID = 11204L;
    private static final long INVENTORY_ID = 11205L;

    @Autowired
    private FulfillmentService fulfillmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PrescriptionAuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        removePreviousTestData();
        insertTestData();

        when(auditRepository.save(any(PrescriptionAudit.class)))
                .thenThrow(new RuntimeException("Simulated audit failure"));
    }

    @Test
    void shouldRollbackAllFulfillmentChangesWhenAuditFails() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fulfillmentService.completed(
                        FULFILLMENT_ID,
                        createPartialFulfillmentRequest()
                )
        );

        assertEquals(
                "Simulated audit failure",
                exception.getMessage()
        );

        String prescriptionStatus =
                jdbcTemplate.queryForObject(
                        """
                        SELECT status
                        FROM prescriptions
                        WHERE id = ?
                        """,
                        String.class,
                        PRESCRIPTION_ID
                );

        assertEquals(
                PrescriptionStatus.SENT_TO_PHARMACY.name(),
                prescriptionStatus
        );

        String fulfillmentStatus =
                jdbcTemplate.queryForObject(
                        """
                        SELECT status
                        FROM prescription_fulfillments
                        WHERE id = ?
                        """,
                        String.class,
                        FULFILLMENT_ID
                );

        assertEquals(
                FulfillmentStatus.READY_FOR_PICKUP.name(),
                fulfillmentStatus
        );

        Integer dispensedQuantity =
                jdbcTemplate.queryForObject(
                        """
                        SELECT dispensed_quantity
                        FROM prescription_items
                        WHERE id = ?
                        """,
                        Integer.class,
                        PRESCRIPTION_ITEM_ID
                );

        assertEquals(0, dispensedQuantity);

        Integer inventoryQuantity =
                jdbcTemplate.queryForObject(
                        """
                        SELECT quantity_available
                        FROM pharmacy_inventory
                        WHERE id = ?
                        """,
                        Integer.class,
                        INVENTORY_ID
                );

        assertEquals(20, inventoryQuantity);

        Integer auditCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM prescription_audit
                        WHERE prescription_id = ?
                        """,
                        Integer.class,
                        PRESCRIPTION_ID
                );

        assertEquals(0, auditCount);
    }

    private com.medtrack.dto.CompleteFulfillmentRequest
    createPartialFulfillmentRequest() {

        com.medtrack.dto.CompleteFulfillmentRequest request =
                new com.medtrack.dto.CompleteFulfillmentRequest();

        com.medtrack.dto.FulfillPrescriptionItemRequest item =
                new com.medtrack.dto.FulfillPrescriptionItemRequest();

        item.setPrescriptionItemId(PRESCRIPTION_ITEM_ID);
        item.setDispensedQuantity(4);

        request.setItems(java.util.List.of(item));

        return request;
    }

    private void insertTestData() {

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                PATIENT_USER_ID,
                "t45-patient@example.com",
                "test-password",
                "ACTIVE"
        );

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                DOCTOR_USER_ID,
                "t45-doctor@example.com",
                "test-password",
                "ACTIVE"
        );

        jdbcTemplate.update(
                """
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                PHARMACY_USER_ID,
                "t45-pharmacy@example.com",
                "test-password",
                "ACTIVE"
        );

        jdbcTemplate.update(
                """
                INSERT INTO patients (
                    id,
                    full_name,
                    user_id,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                PATIENT_ID,
                "T45 Test Patient",
                PATIENT_USER_ID
        );

        jdbcTemplate.update(
                """
                INSERT INTO doctors (
                    id,
                    full_name,
                    license_number,
                    specialization,
                    active,
                    user_id,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                DOCTOR_ID,
                "T45 Test Doctor",
                "T45-LICENSE-001",
                "General Medicine",
                true,
                DOCTOR_USER_ID
        );

        jdbcTemplate.update(
                """
                INSERT INTO pharmacies (
                    id,
                    user_id,
                    name,
                    active,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                PHARMACY_ID,
                PHARMACY_USER_ID,
                "T45 Test Pharmacy",
                true
        );

        jdbcTemplate.update(
                """
                INSERT INTO medications (
                    id,
                    name,
                    active,
                    requires_prescription,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                MEDICATION_ID,
                "T45 Test Medication",
                true,
                true
        );

        jdbcTemplate.update(
                """
                INSERT INTO prescriptions (
                    id,
                    patient_id,
                    doctor_id,
                    status,
                    issue_date,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                PRESCRIPTION_ID,
                PATIENT_ID,
                DOCTOR_ID,
                PrescriptionStatus.SENT_TO_PHARMACY.name()
        );

        jdbcTemplate.update(
                """
                INSERT INTO prescription_items (
                    id,
                    prescription_id,
                    medication_id,
                    dosage,
                    frequency,
                    duration_days,
                    quantity,
                    dispensed_quantity,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                PRESCRIPTION_ITEM_ID,
                PRESCRIPTION_ID,
                MEDICATION_ID,
                "1 tablet",
                "Once daily",
                10,
                10,
                0
        );

        jdbcTemplate.update(
                """
                INSERT INTO pharmacy_inventory (
                    id,
                    pharmacy_id,
                    medication_id,
                    quantity_available
                )
                VALUES (?, ?, ?, ?)
                """,
                INVENTORY_ID,
                PHARMACY_ID,
                MEDICATION_ID,
                20
        );

        jdbcTemplate.update(
                """
                INSERT INTO prescription_fulfillments (
                    id,
                    prescription_id,
                    pharmacy_id,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                FULFILLMENT_ID,
                PRESCRIPTION_ID,
                PHARMACY_ID,
                FulfillmentStatus.READY_FOR_PICKUP.name()
        );
    }

    private void removePreviousTestData() {

        jdbcTemplate.update(
                """
                DELETE FROM prescription_audit
                WHERE prescription_id = ?
                """,
                PRESCRIPTION_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM prescription_fulfillments
                WHERE id = ?
                """,
                FULFILLMENT_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM pharmacy_inventory
                WHERE id = ?
                """,
                INVENTORY_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM prescription_items
                WHERE id = ?
                """,
                PRESCRIPTION_ITEM_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM prescriptions
                WHERE id = ?
                """,
                PRESCRIPTION_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM pharmacies
                WHERE id = ?
                """,
                PHARMACY_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM medications
                WHERE id = ?
                """,
                MEDICATION_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM doctors
                WHERE id = ?
                """,
                DOCTOR_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM patients
                WHERE id = ?
                """,
                PATIENT_ID
        );

        jdbcTemplate.update(
                """
                DELETE FROM users
                WHERE id IN (?, ?, ?)
                """,
                PATIENT_USER_ID,
                DOCTOR_USER_ID,
                PHARMACY_USER_ID
        );
    }
}