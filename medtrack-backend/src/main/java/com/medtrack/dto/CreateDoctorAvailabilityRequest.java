package com.medtrack.dto;

import java.time.LocalDateTime;

public class CreateDoctorAvailabilityRequest {

    private Long doctorId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public CreateDoctorAvailabilityRequest() {
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}