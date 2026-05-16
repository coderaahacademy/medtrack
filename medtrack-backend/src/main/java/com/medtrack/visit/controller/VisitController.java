package com.medtrack.visit.controller;

import com.medtrack.visit.dto.*;
import com.medtrack.visit.service.VisitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visits")
public class VisitController {

    private final VisitService visitService;
    public  VisitController(VisitService visitService) { this.visitService = visitService; }

    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@RequestBody CreateVisitRequest request) {
        VisitResponse body = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<VisitResponse>> getVisitsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VisitResponse> visits = visitService.getVisitsByPatientId(patientId, pageable);
        return ResponseEntity.ok(visits);
    }
}
