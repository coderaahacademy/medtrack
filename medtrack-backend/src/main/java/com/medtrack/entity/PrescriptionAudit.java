package com.medtrack.entity;

import com.medtrack.enums.PrescriptionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_audit")
public class PrescriptionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false, updatable = false)
    private Prescription prescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", updatable = false)
    private PrescriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, updatable = false)
    private PrescriptionStatus newStatus;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "performed_by", updatable = false)
    private String performedBy;

    @Column(name = "description", updatable = false)
    private String description;

    protected PrescriptionAudit() {}

    public PrescriptionAudit(Prescription prescription,
                              PrescriptionStatus previousStatus,
                              PrescriptionStatus newStatus,
                              String performedBy,
                              String description) {
        this.prescription = prescription;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.performedBy = (performedBy != null && !performedBy.isBlank()) ? performedBy : "SYSTEM";
        this.description = description;
        this.eventTimestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Prescription getPrescription() { return prescription; }
    public PrescriptionStatus getPreviousStatus() { return previousStatus; }
    public PrescriptionStatus getNewStatus() { return newStatus; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public String getPerformedBy() { return performedBy; }
    public String getDescription() { return description; }
}
