package com.medtrack.patient.repository;

import com.medtrack.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByUserId(Long userId);
}