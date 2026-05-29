package com.medtrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class InventoryRequest {
    @NotNull(message = "Pharmacy ID is required")
    @Positive(message = "Pharmacy ID must be a positive number")
    private Long pharmacyId;
    @NotNull(message = "Medication ID is required")
    @Positive(message = "Medication ID must be a positive number")
    private Long medicationId;
    @NotNull(message = "Quantity available is required")
    @PositiveOrZero(message = "Quantity available cannot be negative")
    private Integer quantityAvailable;
    @NotNull(message = "Minimum stock is required")
    @PositiveOrZero(message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    public InventoryRequest() {}

    public Long getPharmacyId() {return pharmacyId;}

    public void setPharmacyId(Long pharmacyId) {this.pharmacyId = pharmacyId;}

    public Long getMedicationId() {return medicationId;}

    public void setMedicationId(Long medicationId) {this.medicationId = medicationId;}

    public Integer getQuantityAvailable() {return quantityAvailable;}

    public void setQuantityAvailable(Integer quantityAvailable) {this.quantityAvailable = quantityAvailable;}

    public Integer getMinimumStock() {return minimumStock;}

    public void setMinimumStock(Integer minimumStock) {this.minimumStock = minimumStock;}
}
