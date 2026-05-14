package com.medtrack.patient.repository;

import com.medtrack.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient,Long> {
    boolean existsByUserId(Long userId);
    @Modifying
    @Query("UPDATE Patient p SET p.familyDoctor = null WHERE p.familyDoctor.id = :doctorId")
    void unassignFamilyDoctor(@Param("doctorId") Long doctorId);
}
