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
import com.medtrack.service.TimelineService;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final TimelineService timelineService;
    public PatientController(PatientService patientService, PrescriptionService  prescriptionService , TimelineService timelineService) {
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
        this.timelineService = timelineService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @GetMapping("/{id}/prescriptions")
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptions(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getPrescriptions(id, pageable));
    }

    @PatchMapping("/{id}/family-doctor")
    public ResponseEntity<PatientResponse> updateFamilyDoctor(@PathVariable Long id, @Valid @RequestBody FamilyDoctorRequest request) {
        return ResponseEntity.ok(patientService.updateFamilyDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.delete(id));
    }
    @GetMapping("/{patientId}/timeline")
    public ResponseEntity<List<TimelineItemResponse>> getPatientTimeline(@PathVariable Long patientId) {
        List<TimelineItemResponse> timeline = timelineService.getPatientTimeline(patientId);
        return ResponseEntity.ok(timeline);
    }
}
