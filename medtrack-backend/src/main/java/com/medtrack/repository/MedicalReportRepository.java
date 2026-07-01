package com.medtrack.repository;

import com.medtrack.entity.MedicalReport;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalReportRepository extends BaseRepository<MedicalReport, Long> {
    List<MedicalReport> findByPatientId(Long patientId);
}