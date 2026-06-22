package com.medtrack.controller;

import com.medtrack.dto.MedicationRequest;
import com.medtrack.dto.MedicationResponse;
import com.medtrack.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> create(@Valid @RequestBody MedicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<MedicationResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(medicationService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicationRequest request) {
        return ResponseEntity.ok(medicationService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.deactivate(id));
    }
}