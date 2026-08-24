package com.medtrack.repository;

import com.medtrack.entity.Pharmacy;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PharmacyRepository extends BaseRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}