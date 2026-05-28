package com.medtrack.controller;

import com.medtrack.entity.Medication;
import com.medtrack.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public ResponseEntity<Medication> create(@Valid @RequestBody Medication medication) {
        return ResponseEntity.ok(medicationService.createMedication(medication));
    }

    @GetMapping
    public ResponseEntity<List<Medication>> getAll() {
        return ResponseEntity.ok(medicationService.getAllMedications());
    }
}