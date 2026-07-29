package com.medtrack.dto;

import com.medtrack.enums.PrescriptionStatus;
import jakarta.validation.constraints.NotNull;

public class PrescriptionStatusUpdateRequest {

    @NotNull
    private PrescriptionStatus status;

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }
}