package com.medtrack.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelPrescriptionRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
