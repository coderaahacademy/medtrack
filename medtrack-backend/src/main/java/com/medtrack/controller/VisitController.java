package com.medtrack.controller;

import com.medtrack.dto.CreateVisitRequest;
import com.medtrack.dto.VisitResponse;
import com.medtrack.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService visitService;
    public  VisitController(VisitService visitService) { this.visitService = visitService; }

    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody CreateVisitRequest request) {
        VisitResponse body = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<VisitResponse>> getVisitsByPatientId(
            @PathVariable Long patientId, Pageable pageable) {
        Page<VisitResponse> visits = visitService.getVisitsByPatientId(patientId, pageable);
        return ResponseEntity.ok(visits);
    }

    @PostMapping("/{visitId}/notes")
    public ResponseEntity<Void> createVisitNote(
            @PathVariable Long visitId,
            @RequestBody String note){
        visitService.addNoteToVisit(visitId, note);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{visitId}/notes")
    public ResponseEntity<String> getVisitNotes(@PathVariable Long visitId){
        String currentNote = visitService.getNoteByVisitID(visitId);
        return ResponseEntity.ok(currentNote);

    }
}
