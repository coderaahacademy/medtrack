package com.medtrack.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CompleteFulfillmentRequest {

    @NotEmpty(message = "Fulfillment items are required")
    @Valid
    private List<FulfillPrescriptionItemRequest> items;

    public CompleteFulfillmentRequest() {
    }

    public List<FulfillPrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<FulfillPrescriptionItemRequest> items) {
        this.items = items;
    }
}
