package com.medtrack.repository;

import com.medtrack.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends BaseRepository<Visit, Long> {
    Page<Visit> findByPatientId(Long patientId, Pageable pageable);
    List<Visit> findAllByPatientId(Long patientId);
}
