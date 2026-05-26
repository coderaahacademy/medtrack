package com.medtrack.controller;

import com.medtrack.dto.PrescriptionRequest;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(@RequestBody PrescriptionRequest request) {
        PrescriptionResponse body = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
