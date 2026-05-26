package com.medtrack.repository;

import com.medtrack.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Page<Visit> findByPatientId(Long patientId, Pageable pageable);
}
