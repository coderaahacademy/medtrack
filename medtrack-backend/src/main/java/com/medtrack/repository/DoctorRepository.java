package com.medtrack.repository;

import com.medtrack.entity.Doctor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends BaseRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}