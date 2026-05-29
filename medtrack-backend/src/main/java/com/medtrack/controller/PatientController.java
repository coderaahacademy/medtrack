package com.medtrack.controller;

import com.medtrack.dto.*;
import com.medtrack.service.PatientService;
import com.medtrack.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final PrescriptionService prescriptionService;

    public PatientController(PatientService patientService, PrescriptionService prescriptionService) {
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/{id}/prescriptions")
    public ResponseEntity<Page<PrescriptionResponse>> getPatientPrescriptions(@PathVariable Long id, Pageable pageable) {
        Page<PrescriptionResponse> prescriptions = prescriptionService.getPatientPrescriptions(id, pageable);
        return ResponseEntity.ok(prescriptions);
    }

    @PatchMapping("/{id}/family-doctor")
    public ResponseEntity<PatientResponse> updateFamilyDoctor(@PathVariable Long id, @Valid @RequestBody FamilyDoctorRequest request) {
        PatientResponse body = patientService.updateDoctorFamily(id, request);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.deletePatient(id));
    }
}
