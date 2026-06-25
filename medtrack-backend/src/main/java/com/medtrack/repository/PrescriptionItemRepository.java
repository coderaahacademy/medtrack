package com.medtrack.repository;

import com.medtrack.entity.PrescriptionItem;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionItemRepository extends BaseRepository<PrescriptionItem, Long> {
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
}