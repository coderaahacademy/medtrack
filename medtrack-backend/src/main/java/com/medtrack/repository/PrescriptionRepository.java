package com.medtrack.repository;

import com.medtrack.entity.Prescription;
import com.medtrack.enums.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends BaseRepository<Prescription, Long> {
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);
    List<Prescription> findAllByPatientId(Long patientId);
    Page<Prescription> findAllByPatientIdAndStatus(Long patientId, PrescriptionStatus status, Pageable pageable);
    Page<Prescription> findAllByPatientId(Long patientId, Pageable pageable);
}
