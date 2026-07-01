package com.medtrack.dto;

import java.util.List;

public class PatientTimelineResponse {
    private Long patientId;
    private List<TimelineItemResponse> items;

    public PatientTimelineResponse() {
    }

    public PatientTimelineResponse(Long patientId, List<TimelineItemResponse> items) {
        this.patientId = patientId;
        this.items = items;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public List<TimelineItemResponse> getItems() {
        return items;
    }

    public void setItems(List<TimelineItemResponse> items) {
        this.items = items;
    }
}