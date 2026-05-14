package com.medtrack.doctor.repository;

import com.medtrack.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    boolean existsByUserId(Long userId);
}
