package com.medtrack.repository;

import com.medtrack.entity.DoctorAvailability;

import java.time.LocalDateTime;
import java.util.List;

public interface DoctorAvailabilityRepository extends BaseRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorId(Long doctorId);

    List<DoctorAvailability> findByDoctorIdAndStartAtLessThanAndEndAtGreaterThan(
            Long doctorId,
            LocalDateTime endAt,
            LocalDateTime startAt
    );

    List<DoctorAvailability> findByDoctorIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            Long doctorId,
            LocalDateTime endAt,
            LocalDateTime startAt
    );
}