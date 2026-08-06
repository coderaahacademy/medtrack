package com.medtrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SendToPharmacyRequest {

    @NotNull(message = "Pharmacy ID is required")
    @Positive(message = "Pharmacy ID must be a valid number")
    private Long pharmacyId;

    public SendToPharmacyRequest() {
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }
}
