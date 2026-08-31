package com.medtrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class FulfillPrescriptionItemRequest {

    @NotNull(message = "Prescription item id is required")
    private Long prescriptionItemId;

    @NotNull(message = "Dispensed quantity is required")
    @PositiveOrZero(message = "Dispensed quantity cannot be negative")
    private Integer dispensedQuantity;

    public FulfillPrescriptionItemRequest() {
    }

    public Long getPrescriptionItemId() {
        return prescriptionItemId;
    }

    public void setPrescriptionItemId(Long prescriptionItemId) {
        this.prescriptionItemId = prescriptionItemId;
    }

    public Integer getDispensedQuantity() {
        return dispensedQuantity;
    }

    public void setDispensedQuantity(Integer dispensedQuantity) {
        this.dispensedQuantity = dispensedQuantity;
    }
}
