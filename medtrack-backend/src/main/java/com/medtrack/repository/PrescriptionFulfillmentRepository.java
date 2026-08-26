package com.medtrack.repository;

import com.medtrack.entity.PrescriptionFulfillment;
import jakarta.persistence.LockModeType;
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
}