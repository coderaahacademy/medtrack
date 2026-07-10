package com.medtrack.dto;

import com.medtrack.enums.FulfillmentStatus;

import java.time.LocalDateTime;

public class FulfillmentResponse {

    private Long id;
    private Long prescriptionId;
    private Long pharmacyId;
    private FulfillmentStatus status;
    private LocalDateTime acceptedAt;
    private LocalDateTime readyAt;
    private LocalDateTime completedAt;
    private String rejectionReason;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getPrescriptionId() {
        return prescriptionId;
    }
    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    public Long getPharmacyId() {
        return pharmacyId;
    }
    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
    public FulfillmentStatus getStatus() {
        return status;
    }
    public void setStatus(FulfillmentStatus status) {
        this.status = status;
    }
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(LocalDateTime readyAt) {
        this.readyAt = readyAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    public String getRejectionReason() {
        return rejectionReason;
    }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
