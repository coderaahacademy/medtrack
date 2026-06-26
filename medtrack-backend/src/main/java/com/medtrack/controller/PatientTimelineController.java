package com.medtrack.controller;

import com.medtrack.dto.TimelineItemDTO;
import com.medtrack.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientTimelineController {

    private final TimelineService timelineService;

    public PatientTimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/{patientId}/timeline")
    public ResponseEntity<List<TimelineItemDTO>> getPatientTimeline(@PathVariable Long patientId) {
        List<TimelineItemDTO> timeline = timelineService.getPatientTimeline(patientId);
        return ResponseEntity.ok(timeline);
    }
}