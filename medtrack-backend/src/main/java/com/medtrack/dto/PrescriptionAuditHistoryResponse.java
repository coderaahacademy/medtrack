package com.medtrack.dto;

import com.medtrack.enums.PrescriptionStatus;
import java.time.LocalDateTime;

public class PrescriptionAuditHistoryResponse {

    private Long id;
    private Long prescriptionId;
    private PrescriptionStatus previousStatus;
    private PrescriptionStatus newStatus;
    private LocalDateTime eventTimestamp;
    private String performedBy;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public PrescriptionStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(PrescriptionStatus previousStatus) { this.previousStatus = previousStatus; }

    public PrescriptionStatus getNewStatus() { return newStatus; }
    public void setNewStatus(PrescriptionStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}