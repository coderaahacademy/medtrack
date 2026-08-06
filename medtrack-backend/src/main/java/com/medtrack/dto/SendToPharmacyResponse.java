package com.medtrack.dto;

import com.medtrack.enums.FulfillmentStatus;
import com.medtrack.enums.PrescriptionStatus;

import java.time.LocalDateTime;

public class SendToPharmacyResponse {

    private Long fulfillmentId;
    private Long prescriptionId;
    private Long pharmacyId;
    private PrescriptionStatus prescriptionStatus;
    private FulfillmentStatus fulfillmentStatus;
    private LocalDateTime requestedAt;

    public SendToPharmacyResponse() {
    }

    public Long getFulfillmentId() {
        return fulfillmentId;
    }

    public void setFulfillmentId(Long fulfillmentId) {
        this.fulfillmentId = fulfillmentId;
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

    public PrescriptionStatus getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(PrescriptionStatus prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public FulfillmentStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
