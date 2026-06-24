package com.medtrack.dto;

import jakarta.validation.constraints.NotBlank;


public class RejectFulfillmentRequest {
    @NotBlank(message = "rejectionReason is required")
    private String rejectionReason;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
