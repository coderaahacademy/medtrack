package com.medtrack.entity;

import com.medtrack.enums.PrescriptionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_audit_history")
public class PrescriptionAuditHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private PrescriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PrescriptionStatus newStatus;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "description")
    private String description;

    public PrescriptionAuditHistory() {
    }

    @PrePersist
    protected void onCreate() {
        eventTimestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }

    public PrescriptionStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(PrescriptionStatus previousStatus) { this.previousStatus = previousStatus; }

    public PrescriptionStatus getNewStatus() { return newStatus; }
    public void setNewStatus(PrescriptionStatus newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}