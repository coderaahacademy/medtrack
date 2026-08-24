package com.medtrack.repository;

import com.medtrack.entity.PrescriptionFulfillment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionFulfillmentRepository
        extends BaseRepository<PrescriptionFulfillment, Long> {

    boolean existsByPrescriptionId(Long prescriptionId);

    Optional<PrescriptionFulfillment> findByPrescriptionId(Long prescriptionId);
}
