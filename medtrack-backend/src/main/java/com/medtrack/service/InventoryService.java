package com.medtrack.service;

import com.medtrack.dto.InventoryRequest;
import com.medtrack.dto.InventoryResponse;
import com.medtrack.entity.Medication;
import com.medtrack.entity.Pharmacy;
import com.medtrack.entity.PharmacyInventory;
import com.medtrack.repository.MedicationRepository;
import com.medtrack.repository.InventoryRepository;
import com.medtrack.repository.PharmacyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final MedicationRepository medicationRepository;
    private  final PharmacyRepository pharmacyRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            MedicationRepository medicationRepository,
                            PharmacyRepository pharmacyRepository) {
        this.inventoryRepository = inventoryRepository;
        this.medicationRepository = medicationRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        Long pharmacyId = request.getPharmacyId();
        Long medicationId = request.getMedicationId();
        if (inventoryRepository.existsByPharmacyIdAndMedicationId(pharmacyId, medicationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Inventory record already exists for this pharmacy and medication.");
        }
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Pharmacy not found by id: " + pharmacyId));
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Medication not found by id: " + medicationId));
        PharmacyInventory inventory = new PharmacyInventory();
        inventory.setPharmacy(pharmacy);
        inventory.setMedication(medication);
        inventory.setQuantityAvailable(request.getQuantityAvailable());
        inventory.setMinimumStock(request.getMinimumStock());
        return toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional
    public InventoryResponse update(InventoryRequest request) {
        PharmacyInventory inventory = inventoryRepository.findByPharmacyIdAndMedicationId(request.getPharmacyId(),request.getMedicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Inventory record not found."));
        inventory.setQuantityAvailable(request.getQuantityAvailable());
        inventory.setMinimumStock(request.getMinimumStock());
        return toResponse(inventoryRepository.saveAndFlush(inventory));
    }

    private InventoryResponse toResponse(PharmacyInventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setQuantityAvailable(inventory.getQuantityAvailable());
        response.setMinimumStock(inventory.getMinimumStock());
        response.setUpdatedAt(inventory.getUpdatedAt());
        response.setId(inventory.getId());
        response.setPharmacyId(
                inventory.getPharmacy() != null ? inventory.getPharmacy().getId() : null
        );
        return  response;
    }
}
