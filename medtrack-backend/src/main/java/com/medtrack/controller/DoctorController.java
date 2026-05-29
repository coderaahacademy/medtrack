package com.medtrack.controller;

import com.medtrack.dto.CreateDoctorRequest;
import com.medtrack.dto.DoctorResponse;
import com.medtrack.dto.UpdateDoctorRequest;
import com.medtrack.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@Valid @RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.deleteDoctor(id));
    }
}
