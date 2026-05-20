package com.medtrack.controller;
import com.medtrack.dto.PrescriptionRequest; import com.medtrack.dto.PrescriptionResponse; import com.medtrack.service.PrescriptionService; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/prescriptions")
public class PrescriptionController {
    private final PrescriptionService ps;
    public PrescriptionController(PrescriptionService ps) { this.ps = ps; }
    @PostMapping public ResponseEntity<PrescriptionResponse> create(@RequestBody PrescriptionRequest r) { return new ResponseEntity<>(ps.createPrescription(r), HttpStatus.CREATED); }
    @GetMapping("/{id}") public ResponseEntity<PrescriptionResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(ps.getPrescriptionById(id)); }
    @GetMapping("/patient/{patientId}") public ResponseEntity<List<PrescriptionResponse>> getByPatient(@PathVariable Long patientId) { return ResponseEntity.ok(ps.getPrescriptionsByPatient(patientId)); }
}
