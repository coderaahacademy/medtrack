package com.medtrack.service;

import com.medtrack.dto.MedicationRequest;
import com.medtrack.dto.MedicationResponse;
import com.medtrack.entity.Medication;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.specification.MedicationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Transactional
    public MedicationResponse create(MedicationRequest request) {
        Optional<Medication> existing = getExisting(request);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A medication with this exact profile already exists. ID: " + existing.get().getId());
        }

        Medication medication = new Medication();
        mapRequestToEntity(request, medication);
        return toResponse(medicationRepository.saveAndFlush(medication));
    }

    public Page<MedicationResponse> getAll(Pageable pageable) {
        return medicationRepository.findAllByActiveTrue(pageable).map(this::toResponse);
    }

    public MedicationResponse getById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public MedicationResponse update(Long id, MedicationRequest request) {
        Medication medication = getOrThrow(id);

        Optional<Medication> duplicate = getExisting(request);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Update failed. Another medication already exists with this exact profile. ID: " + duplicate.get().getId());
        }

        mapRequestToEntity(request, medication);
        return toResponse(medicationRepository.saveAndFlush(medication));
    }

    @Transactional
    public String deactivate(Long id) {
        Medication medication = getOrThrow(id);
        medication.setActive(false);
        medicationRepository.saveAndFlush(medication);
        return "Medication ID " + id + " was successfully deactivated.";
    }

    @Transactional(readOnly = true)
    public Page<MedicationResponse> search(String q, String name, String genericName, String brand, Pageable pageable) {
        Specification<Medication> spec = MedicationSpecification.search(q, name, genericName, brand);
        return medicationRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private Optional<Medication> getExisting(MedicationRequest request) {
        return medicationRepository.findFirstByNameIgnoreCaseAndGenericNameIgnoreCaseAndBrandIgnoreCaseAndDosageFormIgnoreCaseAndStrengthIgnoreCase(
                request.getName(), request.getGenericName(), request.getBrand(), request.getDosageForm(), request.getStrength());
    }

    private Medication getOrThrow(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found with id: " + id));
    }

    private void mapRequestToEntity(MedicationRequest request, Medication medication) {
        medication.setName(request.getName());
        medication.setGenericName(request.getGenericName());
        medication.setBrand(request.getBrand());
        medication.setDosageForm(request.getDosageForm());
        medication.setStrength(request.getStrength());
        medication.setRequiresPrescription(request.isRequiresPrescription());
        medication.setActive(request.isActive());
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