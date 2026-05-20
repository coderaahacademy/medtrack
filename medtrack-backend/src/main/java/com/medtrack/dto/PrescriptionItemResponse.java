package com.medtrack.dto;
import java.time.LocalDateTime;
public class PrescriptionItemResponse {
    private Long id; private Long medicationId; private String medicationName; private String dosage; private String frequency; private Integer durationDays; private Integer quantity; private String instructions; private LocalDateTime createdAt; private LocalDateTime updatedAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getMedicationId() { return medicationId; } public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    public String getMedicationName() { return medicationName; } public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; } public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; } public void setFrequency(String frequency) { this.frequency = frequency; }
    public Integer getDurationDays() { return durationDays; } public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getInstructions() { return instructions; } public void setInstructions(String instructions) { this.instructions = instructions; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
