package com.medtrack.dto;

import com.medtrack.enums.PrescriptionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionRequest {

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be a valid number")
    private Long patientId;
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be a valid number")
    private Long doctorId;
    @Positive(message = "Visit ID must be a valid number")
    private Long visitId;
    @NotEmpty(message = "A prescription must contain at least one medication item")
    @Valid
    private List<PrescriptionItemRequest> items;
    @NotNull(message = "Prescription status is required")
    private PrescriptionStatus status;
    @NotNull(message = "Issue date is required")
    @PastOrPresent(message = "Issue date cannot be in the future")
    private LocalDateTime issueDate;
    private String notes;

    public PrescriptionRequest() {}

    public Long getPatientId() {return patientId;}

    public void setPatientId(Long patientId) {this.patientId = patientId;}

    public Long getDoctorId() {return doctorId;}

    public void setDoctorId(Long doctorId) {this.doctorId = doctorId;}

    public Long getVisitId() {return visitId;}

    public void setVisitId(Long visitId) {this.visitId = visitId;}

    public List<PrescriptionItemRequest> getItems() {return items;}

    public void setItems(List<PrescriptionItemRequest> items) {this.items = items;}

    public PrescriptionStatus getStatus() {return status;}

    public void setStatus(PrescriptionStatus status) {this.status = status;}

    public LocalDateTime getIssueDate() {return issueDate;}

    public void setIssueDate(LocalDateTime issueDate) {this.issueDate = issueDate;}

    public String getNotes() {return notes;}

    public void setNotes(String notes) {this.notes = notes;}
}
