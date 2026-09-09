package com.medtrack.repository;

import com.medtrack.entity.PrescriptionFulfillment;
import com.medtrack.enums.FulfillmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionFulfillmentRepository
        extends BaseRepository<PrescriptionFulfillment, Long> {

    boolean existsByPrescriptionId(Long prescriptionId);

    Optional<PrescriptionFulfillment> findByPrescriptionId(Long prescriptionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT f
        FROM PrescriptionFulfillment f
        JOIN FETCH f.prescription p
        WHERE f.id = :id
    """)
    Optional<PrescriptionFulfillment> findForUpdate(
            @Param("id") Long id
    );

    /**
     * T46: Pharmacy work queue - fulfillments assigned to one pharmacy.
     */
    @EntityGraph(attributePaths = {"prescription", "prescription.patient", "pharmacy"})
    Page<PrescriptionFulfillment> findByPharmacyId(Long pharmacyId, Pageable pageable);

    /**
     * T46: Pharmacy work queue filtered by fulfillment status.
     */
    @EntityGraph(attributePaths = {"prescription", "prescription.patient", "pharmacy"})
    Page<PrescriptionFulfillment> findByPharmacyIdAndStatus(Long pharmacyId,
                                                            FulfillmentStatus status,
                                                            Pageable pageable);

    /**
     * T46: Detail view - loads the full object graph needed for dispensing in one query.
     */
    @Query("""
        SELECT f
        FROM PrescriptionFulfillment f
        JOIN FETCH f.pharmacy
        JOIN FETCH f.prescription p
        JOIN FETCH p.patient
        LEFT JOIN FETCH p.items i
        LEFT JOIN FETCH i.medication
        WHERE f.id = :id
    """)
    Optional<PrescriptionFulfillment> findDetailById(@Param("id") Long id);
}
