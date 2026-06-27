package com.medtrack.controller;

import com.medtrack.entity.Pharmacy;
import com.medtrack.service.PharmacyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {
    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @PostMapping
    public ResponseEntity<Pharmacy> create(@RequestBody Pharmacy pharmacy) {
        return ResponseEntity.ok(pharmacyService.createPharmacy(pharmacy));
    }

    @GetMapping
    public ResponseEntity<List<Pharmacy>> getAll() {
        return ResponseEntity.ok(pharmacyService.getAllPharmacies());
    }
}