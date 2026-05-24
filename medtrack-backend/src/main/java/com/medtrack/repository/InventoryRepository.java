package com.medtrack.repository;

import com.medtrack.entity.PharmacyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<PharmacyInventory, Long> {

    List<PharmacyInventory> findByPharmacyId(Long pharmacyId);

    Optional<PharmacyInventory> findByPharmacyIdAndMedicationId(Long pharmacyId, Long medicationId);

    boolean existsByPharmacyIdAndMedicationId(Long pharmacyId, Long medicationId);
}