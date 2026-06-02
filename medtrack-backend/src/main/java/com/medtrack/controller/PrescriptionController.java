package com.medtrack.controller;

import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        PrescriptionResponse body = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<Page<PrescriptionResponse>> getAllPrescriptions(Pageable pageable){
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions(pageable));

    }
}
