package com.medtrack.controller;

import com.medtrack.dto.VisitRequest;
import com.medtrack.dto.VisitResponse;
import com.medtrack.service.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@RequestBody VisitRequest request) {
        return new ResponseEntity<>(visitService.createVisit(request), HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<VisitResponse>> getVisitsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(visitService.getVisitsByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisitById(id));
    }
}
