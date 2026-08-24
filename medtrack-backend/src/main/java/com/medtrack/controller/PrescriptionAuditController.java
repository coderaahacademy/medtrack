package com.medtrack.controller;

import com.medtrack.dto.PrescriptionAuditResponse;
import com.medtrack.service.PrescriptionAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@Tag(name = "Prescription Audit", description = "Prescription audit history endpoints")
public class PrescriptionAuditController {

    private final PrescriptionAuditService auditService;

    public PrescriptionAuditController(PrescriptionAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{id}/audit-history")
    @PreAuthorize("@authz.canReadPrescriptionAudit(#id)")
    @Operation(summary = "Get audit history for a prescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit history returned"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Caller cannot access this prescription audit history"),
            @ApiResponse(responseCode = "404", description = "Prescription not found")
    })
    public ResponseEntity<List<PrescriptionAuditResponse>> getAuditHistory(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getHistory(id));
    }
}
