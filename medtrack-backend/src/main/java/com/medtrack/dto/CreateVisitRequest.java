package com.medtrack.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class CreateVisitRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be a positive number")
    private Long patientId;
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be a positive number")
    private Long doctorId;
    @NotNull(message = "Visit date is required")
    @FutureOrPresent(message = "Visit date must be today or in the future")
    private LocalDateTime visitDate;
    private String symptoms;
    private String diagnosis;
    private String notes;
    @Future(message = "Follow-up date must be in the future")
    private LocalDateTime followUpDate;

    public CreateVisitRequest() {}

    public Long getPatientId() { return patientId; }

    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }

    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getVisitDate() { return visitDate; }

    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

    public String getSymptoms() { return symptoms; }

    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }

    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getFollowUpDate() { return followUpDate; }

    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }
}
