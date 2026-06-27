package com.medtrack.repository;

import com.medtrack.entity.PrescriptionFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionFulfillmentRepository extends JpaRepository<PrescriptionFulfillment, Long> {
}