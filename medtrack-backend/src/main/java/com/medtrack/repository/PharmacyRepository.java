package com.medtrack.repository;

import com.medtrack.entity.Pharmacy;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacyRepository extends BaseRepository<Pharmacy, Long> {
}