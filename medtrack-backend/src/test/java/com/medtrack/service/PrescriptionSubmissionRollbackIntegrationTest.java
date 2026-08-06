package com.medtrack.service;

import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.repository.PharmacyRepository;
import com.medtrack.repository.PrescriptionFulfillmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class PrescriptionSubmissionRollbackIntegrationTest {

    private static final long PATIENT_USER_ID = 9101L;
    private static final long DOCTOR_USER_ID = 9102L;
    private static final long PATIENT_ID = 9201L;
    private static final long DOCTOR_ID = 9202L;
    private static final long PRESCRIPTION_ID = 9301L;
    private static final long PHARMACY_ID = 9401L;

    @Autowired
    private PrescriptionSubmissionService submissionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PharmacyRepository pharmacyRepository;

    @MockitoBean
    private PrescriptionFulfillmentRepository fulfillmentRepository;

    @BeforeEach
    void setUp() {
        removePreviousTestData();
        insertPrescriptionTestData();

        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setId(PHARMACY_ID);
        pharmacy.setName("Rollback Test Pharmacy");
        pharmacy.setActive(true);

        when(pharmacyRepository.findById(PHARMACY_ID))
                .thenReturn(Optional.of(pharmacy));

        when(fulfillmentRepository.existsByPrescriptionId(
                PRESCRIPTION_ID
        )).thenReturn(false);

        when(fulfillmentRepository.save(
                any(PrescriptionFulfillment.class)
        )).thenThrow(
                new RuntimeException("Simulated fulfillment failure")
        );
    }

    @Test
    void shouldRollbackStatusWhenFulfillmentSaveFails() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> submissionService.sendToPharmacy(
                        PRESCRIPTION_ID,
                        PHARMACY_ID
                )
        );

        assertEquals(
                "Simulated fulfillment failure",
                exception.getMessage()
        );

        PrescriptionStatus storedStatus =
                jdbcTemplate.queryForObject(
                        """
                        SELECT status
                        FROM prescriptions
                        WHERE id = ?
                        """,
                        (resultSet, rowNumber) ->
                                PrescriptionStatus.valueOf(
                                        resultSet.getString("status")
                                ),
                        PRESCRIPTION_ID
                );

        assertEquals(
                PrescriptionStatus.ISSUED,
                storedStatus
        );
    }

    private void removePreviousTestData() {
        jdbcTemplate.update(
                """
                DELETE FROM prescription_fulfillments
                WHERE prescription_id = ?
                """,
                PRESCRIPTION_ID
        );

        jdbcTemplate.update(
                "DELETE FROM prescriptions WHERE id = ?",
                PRESCRIPTION_ID
        );

        jdbcTemplate.update(
                "DELETE FROM doctors WHERE id = ?",
                DOCTOR_ID
        );

        jdbcTemplate.update(
                "DELETE FROM patients WHERE id = ?",
                PATIENT_ID
        );

        jdbcTemplate.update(
                "DELETE FROM users WHERE id IN (?, ?)",
                PATIENT_USER_ID,
                DOCTOR_USER_ID
        );
    }

    private void insertPrescriptionTestData() {
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
                "t37-patient@example.com",
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
                "t37-doctor@example.com",
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
                "T37 Test Patient",
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
                "T37 Test Doctor",
                "T37-LICENSE-001",
                "General Medicine",
                true,
                DOCTOR_USER_ID
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
                "ISSUED"
        );
    }
}
