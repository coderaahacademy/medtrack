package com.medtrack.controller;

import com.medtrack.dto.InventoryRequest;
import com.medtrack.dto.InventoryResponse;
import com.medtrack.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies/{pharmacyId}/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(@PathVariable Long pharmacyId, @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(pharmacyId, request));
    }

    @PutMapping("/{medicationId}")
    public ResponseEntity<InventoryResponse> update(@PathVariable Long pharmacyId, @PathVariable Long medicationId, @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.update(pharmacyId, medicationId, request));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockInventory(@PathVariable Long pharmacyId) {
        return ResponseEntity.ok(inventoryService.getLowStockInventory(pharmacyId));
    }
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getByPharmacy(
            @PathVariable Long pharmacyId) {

        return ResponseEntity.ok(
                inventoryService.getByPharmacy(pharmacyId)
        );
    }
}