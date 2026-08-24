package com.medtrack.controller;

import com.medtrack.dto.*;
import com.medtrack.enums.PrescriptionStatus;
import com.medtrack.service.PatientService;
import com.medtrack.service.PrescriptionService;
import com.medtrack.service.TimelineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final PrescriptionService prescriptionService;
    private final TimelineService timelineService;

    public PatientController(PatientService patientService, PrescriptionService prescriptionService, TimelineService timelineService) {
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
        this.timelineService = timelineService;
    }

    @PostMapping
    @PreAuthorize("@authz.canCreatePatient(#request.userId)")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PatientResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authz.canAccessPatient(#id)")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authz.canModifyPatient(#id)")
    public ResponseEntity<PatientResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @GetMapping("/{patientId}/prescriptions")
    @PreAuthorize("@authz.canAccessPatientPrescriptions(#patientId)")
    public ResponseEntity<Page<PrescriptionResponse>> getPatientPrescriptions(@PathVariable Long patientId, @RequestParam(required = false) PrescriptionStatus status, Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(patientId, status, pageable));
    }

    @PatchMapping("/{id}/family-doctor")
    @PreAuthorize("@authz.canModifyPatient(#id)")
    public ResponseEntity<PatientResponse> updateFamilyDoctor(@PathVariable Long id, @Valid @RequestBody FamilyDoctorRequest request) {
        return ResponseEntity.ok(patientService.updateFamilyDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.canModifyPatient(#id)")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.delete(id));
    }

    @GetMapping("/{patientId}/timeline")
    @PreAuthorize("@authz.canAccessPatientTimeline(#patientId)")
    public ResponseEntity<List<TimelineItemResponse>> getPatientTimeline(@PathVariable Long patientId) {
        List<TimelineItemResponse> timeline = timelineService.getPatientTimeline(patientId);
        return ResponseEntity.ok(timeline);
    }
}
