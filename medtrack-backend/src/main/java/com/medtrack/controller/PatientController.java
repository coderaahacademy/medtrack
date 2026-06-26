package com.medtrack.controller;

import com.medtrack.dto.FamilyDoctorRequest;
import com.medtrack.dto.PatientRequest;
import com.medtrack.dto.PatientResponse;
import com.medtrack.dto.PrescriptionResponse;
import com.medtrack.service.PatientService;
import com.medtrack.dto.TimelineItemDTO;
import com.medtrack.service.PrescriptionService;
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
    public ResponseEntity<PatientResponse> createPatient(@RequestBody PatientRequest request) {
        return new ResponseEntity<>(patientService.createPatient(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAllPatients(pageable));
    }

    @GetMapping("/{id}/prescriptions")
    public ResponseEntity<Page<PrescriptionResponse>> getPatientPrescriptions(@PathVariable Long id, Pageable pageable) {
        Page<PrescriptionResponse> prescriptions = prescriptionService.getPatientPrescriptions(id, pageable);
        return ResponseEntity.ok(prescriptions);
    }

    @PatchMapping("/{id}/family-doctor")
    public ResponseEntity<PatientResponse> updateFamilyDoctor(@PathVariable Long id, @RequestBody FamilyDoctorRequest request) {
        PatientResponse body = patientService.updateDoctorFamily(id, request);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();

    }
    // 🎫 متد اختصاصی تسک T32 (به داخل بدنه اصلی کلاس منتقل شد)
    @GetMapping("/{patientId}/timeline")
    public ResponseEntity<List<TimelineItemDTO>> getPatientTimeline(@PathVariable Long patientId) {
        List<TimelineItemDTO> timeline = patientService.getPatientTimeline(patientId);
        return ResponseEntity.ok(timeline);
    }
}
