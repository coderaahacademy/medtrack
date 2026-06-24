package com.medtrack.controller;

import com.medtrack.dto.CreateVisitRequest;
import com.medtrack.dto.NotesResponse;
import com.medtrack.dto.VisitResponse;
import com.medtrack.dto.NotesRequest;
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
    public ResponseEntity<VisitResponse> create(@Valid @RequestBody CreateVisitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.create(request));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<VisitResponse>> getByPatientId(
            @PathVariable Long patientId, Pageable pageable) {
        return ResponseEntity.ok(visitService.getByPatientId(patientId, pageable));
    }

    @PostMapping("/{visitId}/notes")
    public ResponseEntity<NotesResponse> createVisitNote(
            @PathVariable Long visitId,
            @Valid @RequestBody NotesRequest request){
        NotesResponse response = visitService.addNoteToVisit(visitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{visitId}/notes")
    public ResponseEntity<NotesResponse> getVisitNotes(@PathVariable Long visitId){
        NotesResponse response = visitService.getNoteByVisitID(visitId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<VisitResponse> getById(@PathVariable Long visitId) {
        return ResponseEntity.ok(visitService.getById(visitId));
    }
}
