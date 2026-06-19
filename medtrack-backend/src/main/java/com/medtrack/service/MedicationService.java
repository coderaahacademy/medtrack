package com.medtrack.service;

import com.medtrack.dto.MedicationRequest;
import com.medtrack.dto.MedicationResponse;
import com.medtrack.entity.Medication;
import com.medtrack.repository.MedicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Transactional
    public MedicationResponse create(MedicationRequest request) {
        Medication medication = new Medication();
        medication.setName(request.getName());
        medication.setGenericName(request.getGenericName());
        medication.setBrand(request.getBrand());
        medication.setDosageForm(request.getDosageForm());
        medication.setStrength(request.getStrength());
        medication.setRequiresPrescription(request.isRequiresPrescription());
        medication.setActive(request.isActive());
        return toResponse(medicationRepository.saveAndFlush(medication));
    }

    public Page<MedicationResponse> getAll(Pageable pageable) {
        return medicationRepository.findAll(pageable).map(this::toResponse);
    }

    private MedicationResponse toResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setName(medication.getName());
        response.setGenericName(medication.getGenericName());
        response.setBrand(medication.getBrand());
        response.setDosageForm(medication.getDosageForm());
        response.setStrength(medication.getStrength());
        response.setRequiresPrescription(medication.isRequiresPrescription());
        response.setActive(medication.isActive());
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());
        response.setId(medication.getId());
        return response;
    }
}