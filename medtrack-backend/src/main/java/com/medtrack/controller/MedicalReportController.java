package com.medtrack.controller;

import com.medtrack.dto.CreateMedicalReportRequest;
import com.medtrack.dto.MedicalReportResponse;
import com.medtrack.service.MedicalReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medical-reports")
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authz.canCreateMedicalReport(#request.doctorId)")
    public MedicalReportResponse create(
            @Valid @RequestBody CreateMedicalReportRequest request) {

        return medicalReportService.create(request);
    }
}
