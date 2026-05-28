package com.medtrack.dto;

public class InventoryRequest {
    private Long pharmacyId;
    private Long medicationId;
    private Integer quantityAvailable;
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
