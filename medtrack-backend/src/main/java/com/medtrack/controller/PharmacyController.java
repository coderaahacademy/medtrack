package com.medtrack.controller;

import com.medtrack.dto.PharmacyRequest;
import com.medtrack.dto.PharmacyResponse;
import com.medtrack.service.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {
    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @PostMapping
    public ResponseEntity<PharmacyResponse> create(@Valid @RequestBody PharmacyRequest request) {
        PharmacyResponse body = pharmacyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Page<PharmacyResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(pharmacyService.getAll(pageable));
    }
}