package com.medtrack.dto;

import java.time.LocalDateTime;

public class InventoryResponse {
    private Long id;
    private Long pharmacyId;
    private Long medicationId;
    private Integer quantityAvailable;
    private Integer minimumStock;
    private LocalDateTime updatedAt;

    public InventoryResponse() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Long getPharmacyId() {return pharmacyId;}

    public void setPharmacyId(Long pharmacyId) {this.pharmacyId = pharmacyId;}

    public Long getMedicationId() {return medicationId;}

    public void setMedicationId(Long medicationId) {this.medicationId = medicationId;}

    public Integer getQuantityAvailable() {return quantityAvailable;}

    public void setQuantityAvailable(Integer quantityAvailable) {this.quantityAvailable = quantityAvailable;}

    public Integer getMinimumStock() {return minimumStock;}

    public void setMinimumStock(Integer minimumStock) {this.minimumStock = minimumStock;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
