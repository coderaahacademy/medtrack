package com.medtrack.prescription.repository;

import com.medtrack.prescription.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);
}
