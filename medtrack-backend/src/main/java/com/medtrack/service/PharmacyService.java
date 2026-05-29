package com.medtrack.service;

import com.medtrack.dto.PharmacyRequest;
import com.medtrack.dto.PharmacyResponse;
import com.medtrack.entity.Pharmacy;
import com.medtrack.repository.PharmacyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PharmacyService {
    private final PharmacyRepository pharmacyRepository;

    public PharmacyService(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    public PharmacyResponse create(PharmacyRequest request) {
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(request.getName());
        pharmacy.setAddress(request.getAddress());
        pharmacy.setPhone(request.getPhone());
        pharmacy.setEmail(request.getEmail());
        pharmacy.setActive(request.isActive());
        Pharmacy saved = pharmacyRepository.saveAndFlush(pharmacy);
        return toResponse(saved);
    }

    public Page<PharmacyResponse> getAll(Pageable pageable){
        return pharmacyRepository.findAll(pageable).map(this::toResponse);
    }

    private PharmacyResponse toResponse(Pharmacy pharmacy) {
        PharmacyResponse response = new PharmacyResponse();
        response.setName(pharmacy.getName());
        response.setAddress(pharmacy.getAddress());
        response.setPhone(pharmacy.getPhone());
        response.setEmail(pharmacy.getEmail());
        response.setActive(pharmacy.isActive());
        response.setCreatedAt(pharmacy.getCreatedAt());
        response.setUpdatedAt(pharmacy.getUpdatedAt());
        response.setId(pharmacy.getId());
        return response;
    }
}