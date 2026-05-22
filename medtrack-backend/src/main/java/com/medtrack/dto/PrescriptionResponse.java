package com.medtrack.dto;

import com.medtrack.enums.PrescriptionStatus;
import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long visitId;
    private List<PrescriptionItemResponse> items;
    private PrescriptionStatus status;
    private LocalDateTime issueDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PrescriptionResponse() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Long getPatientId() {return patientId;}

    public void setPatientId(Long patientId) {this.patientId = patientId;}

    public Long getDoctorId() {return doctorId;}

    public void setDoctorId(Long doctorId) {this.doctorId = doctorId;}

    public Long getVisitId() {return visitId;}

    public void setVisitId(Long visitId) {this.visitId = visitId;}

    public List<PrescriptionItemResponse> getItems() {return items;}

    public void setItems(List<PrescriptionItemResponse> items) {this.items = items;}

    public PrescriptionStatus getStatus() {return status;}

    public void setStatus(PrescriptionStatus status) {this.status = status;}

    public LocalDateTime getIssueDate() {return issueDate;}

    public void setIssueDate(LocalDateTime issueDate) {this.issueDate = issueDate;}

    public String getNotes() {return notes;}

    public void setNotes(String notes) {this.notes = notes;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
