package com.medtrack.repository;

import com.medtrack.entity.Prescription;
import com.medtrack.enums.PrescriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository
        extends BaseRepository<Prescription, Long> {

    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);

    List<Prescription> findAllByPatientId(Long patientId);

    Page<Prescription> findAllByPatientIdAndStatus(
            Long patientId,
            PrescriptionStatus status,
            Pageable pageable
    );

    Page<Prescription> findAllByPatientId(
            Long patientId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prescription p where p.id = :id")
    Optional<Prescription> findByIdForUpdate(@Param("id") Long id);
}
