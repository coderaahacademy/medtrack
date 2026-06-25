package com.medtrack.repository;

import com.medtrack.entity.Patient;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends BaseRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    @Modifying
    @Query("UPDATE Patient p SET p.familyDoctor = null WHERE p.familyDoctor.id = :doctorId")
    void unassignFamilyDoctor(@Param("doctorId") Long doctorId);
}