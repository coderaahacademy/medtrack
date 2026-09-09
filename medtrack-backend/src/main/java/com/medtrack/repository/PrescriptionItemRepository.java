package com.medtrack.repository;

import com.medtrack.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionItemRepository extends BaseRepository<PrescriptionItem, Long> {
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    /**
     * T46: Item counts for a whole page of prescriptions in ONE query,
     * so the pharmacy queue does not trigger a lazy load per row.
     */
    @Query("""
        SELECT i.prescription.id, COUNT(i)
        FROM PrescriptionItem i
        WHERE i.prescription.id IN :prescriptionIds
        GROUP BY i.prescription.id
    """)
    List<Object[]> countItemsByPrescriptionIds(@Param("prescriptionIds") List<Long> prescriptionIds);
}