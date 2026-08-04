package com.medtrack.repository;

import com.medtrack.entity.PrescriptionAuditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionAuditHistoryRepository
        extends JpaRepository<PrescriptionAuditHistory, Long> {

    List<PrescriptionAuditHistory> findByPrescriptionIdOrderByEventTimestampAsc(Long prescriptionId);

    boolean existsByPrescriptionId(Long prescriptionId);
}