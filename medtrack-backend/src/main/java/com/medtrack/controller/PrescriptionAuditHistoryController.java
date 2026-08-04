package com.medtrack.controller;

import com.medtrack.dto.PrescriptionAuditHistoryResponse;
import com.medtrack.service.PrescriptionAuditHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionAuditHistoryController {

    private final PrescriptionAuditHistoryService auditHistoryService;

    public PrescriptionAuditHistoryController(PrescriptionAuditHistoryService auditHistoryService) {
        this.auditHistoryService = auditHistoryService;
    }

    @GetMapping("/{id}/audit-history")
    public ResponseEntity<List<PrescriptionAuditHistoryResponse>> getAuditHistory(@PathVariable Long id) {
        return ResponseEntity.ok(auditHistoryService.getAuditHistory(id));
    }
}