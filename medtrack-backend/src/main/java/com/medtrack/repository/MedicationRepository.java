package com.medtrack.repository;

import com.medtrack.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Optional<Medication> findFirstByNameIgnoreCaseAndGenericNameIgnoreCaseAndBrandIgnoreCaseAndDosageFormIgnoreCaseAndStrengthIgnoreCase(
            String name, String genericName, String brand, String dosageForm, String strength
    );

    Page<Medication> findAllByActiveTrue(Pageable pageable);
}
