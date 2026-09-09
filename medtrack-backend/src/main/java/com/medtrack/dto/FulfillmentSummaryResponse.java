package com.medtrack.dto;

import com.medtrack.enums.FulfillmentStatus;

import java.time.LocalDateTime;

/**
 * T46: One row of the pharmacy work queue.
 * Deliberately slim - the queue is a list view, the detail endpoint carries the dispensing data.
 */
public class FulfillmentSummaryResponse {

    private Long id;
    private Long prescriptionId;
    private Long patientId;
    private String patientName;
    private FulfillmentStatus status;
    private int itemCount;
    private LocalDateTime requestedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FulfillmentSummaryResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public FulfillmentStatus getStatus() { return status; }
    public void setStatus(FulfillmentStatus status) { this.status = status; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
