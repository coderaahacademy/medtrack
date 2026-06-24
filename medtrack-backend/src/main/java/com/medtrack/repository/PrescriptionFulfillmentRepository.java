package com.medtrack.repository;

import com.medtrack.entity.PrescriptionFulfillment;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionFulfillmentRepository extends BaseRepository<PrescriptionFulfillment, Long> {
}