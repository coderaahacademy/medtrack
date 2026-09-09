package com.medtrack.dto;

import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * T46: Everything pharmacy staff needs to process one prescription.
 */
public class FulfillmentDetailResponse {

    private Long id;
    private Long pharmacyId;
    private String pharmacyName;
    private FulfillmentStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime readyAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String rejectionReason;

    private Long prescriptionId;
    private PrescriptionStatus prescriptionStatus;
    private LocalDateTime issueDate;
    private String prescriptionNotes;

    private Long patientId;
    private String patientName;
    private LocalDate patientBirthDate;
    private String patientAllergies;

    private List<FulfillmentItemResponse> items = new ArrayList<>();

    public FulfillmentDetailResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(Long pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public FulfillmentStatus getStatus() { return status; }
    public void setStatus(FulfillmentStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getReadyAt() { return readyAt; }
    public void setReadyAt(LocalDateTime readyAt) { this.readyAt = readyAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public PrescriptionStatus getPrescriptionStatus() { return prescriptionStatus; }
    public void setPrescriptionStatus(PrescriptionStatus prescriptionStatus) { this.prescriptionStatus = prescriptionStatus; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public String getPrescriptionNotes() { return prescriptionNotes; }
    public void setPrescriptionNotes(String prescriptionNotes) { this.prescriptionNotes = prescriptionNotes; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public LocalDate getPatientBirthDate() { return patientBirthDate; }
    public void setPatientBirthDate(LocalDate patientBirthDate) { this.patientBirthDate = patientBirthDate; }

    public String getPatientAllergies() { return patientAllergies; }
    public void setPatientAllergies(String patientAllergies) { this.patientAllergies = patientAllergies; }

    public List<FulfillmentItemResponse> getItems() { return items; }
    public void setItems(List<FulfillmentItemResponse> items) { this.items = items; }
}
