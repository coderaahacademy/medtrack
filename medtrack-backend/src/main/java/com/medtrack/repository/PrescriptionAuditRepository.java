package com.medtrack.repository;

import com.medtrack.entity.PrescriptionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionAuditRepository extends JpaRepository<PrescriptionAudit, Long> {
    List<PrescriptionAudit> findByPrescription_IdOrderByEventTimestampAsc(Long prescriptionId);
}
