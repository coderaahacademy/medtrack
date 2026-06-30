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
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/inventory")
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @PutMapping("/inventory")
    public ResponseEntity<InventoryResponse> update(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.update(request));
    }

    @GetMapping("/pharmacies/{pharmacyId}/inventory/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockInventory(@PathVariable Long pharmacyId) {
        return ResponseEntity.ok(inventoryService.getLowStockInventory(pharmacyId));
    }
}
