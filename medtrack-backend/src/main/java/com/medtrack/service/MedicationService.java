package com.medtrack.service;

import com.medtrack.entity.Medication;
import com.medtrack.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public Medication createMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }
}