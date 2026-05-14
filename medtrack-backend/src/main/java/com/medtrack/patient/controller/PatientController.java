package com.medtrack.patient.controller;

import com.medtrack.patient.dto.*;
import com.medtrack.patient.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;
    public PatientController(PatientService patientService ) {
        this.patientService = patientService;
    }
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@RequestBody CreatePatientRequest request) {
        PatientResponse body = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @RequestBody UpdatePatientRequest request) {
        PatientResponse body = patientService.updatePatient(id, request);
        return ResponseEntity.ok(body);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        PatientResponse body = patientService.getPatientById(id);
        return ResponseEntity.ok(body);
    }
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PatientResponse> body = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(body);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<PatientResponse> deletePatient(@PathVariable Long id) {
        PatientResponse body = patientService.deletePatient(id);
        return ResponseEntity.ok(body);
    }
}
