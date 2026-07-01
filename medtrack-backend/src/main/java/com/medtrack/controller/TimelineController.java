package com.medtrack.controller;

import com.medtrack.dto.PatientTimelineResponse;
import com.medtrack.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class TimelineController {

    private final TimelineService timelineService;


    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/{patientId}/timeline")
    public ResponseEntity<PatientTimelineResponse> getPatientTimeline(@PathVariable Long patientId) {
        PatientTimelineResponse response = timelineService.getPatientTimeline(patientId);
        return ResponseEntity.ok(response);
    }
}