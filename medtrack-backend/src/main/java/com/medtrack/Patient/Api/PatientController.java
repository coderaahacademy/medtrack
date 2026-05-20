package com.medtrack.patient.api;

import com.medtrack.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PutMapping("/{patientId}/family-doctor")
    public ResponseEntity<String> assignFamilyDoctor(
            @PathVariable Long patientId,
            @RequestParam Long doctorId) {

        patientService.assignFamilyDoctor(patientId, doctorId);

        return ResponseEntity.ok("Family doctor assigned successfully");
    }
}