package com.medtrack.repository;

import com.medtrack.entity.PharmacyInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends BaseRepository<PharmacyInventory, Long> {

    List<PharmacyInventory> findByPharmacyId(Long pharmacyId);

    Optional<PharmacyInventory> findByPharmacyIdAndMedicationId(Long pharmacyId, Long medicationId);

    boolean existsByPharmacyIdAndMedicationId(Long pharmacyId, Long medicationId);

    @Query("""
        SELECT i
        FROM PharmacyInventory i
        WHERE i.pharmacy.id = :pharmacyId
          AND i.minimumStock IS NOT NULL
          AND i.quantityAvailable <= i.minimumStock
    """)
    List<PharmacyInventory> findLowStockByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}