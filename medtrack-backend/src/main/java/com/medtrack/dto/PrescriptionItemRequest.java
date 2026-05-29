package com.medtrack.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PrescriptionItemRequest {

    @NotNull(message = "Medication ID is required")
    @Positive(message = "Medication ID must be a positive number")
    private Long medicationId;
    @NotBlank(message = "Dosage is required (e.g., 500mg)")
    private String dosage;
    @NotBlank(message = "Frequency is required (e.g., Twice a day)")
    private String frequency;
    @NotNull(message = "Duration in days is required")
    @Positive(message = "Duration must be at least 1 day")
    @Max(value = 365, message = "Duration cannot exceed 1 year (365 days) for a single prescription")
    private Integer durationDays;
    @NotNull(message = "Quantity to dispense is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
    private String instructions;

    public PrescriptionItemRequest() {}

    public Long getMedicationId() { return medicationId; }

    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }

    public String getDosage() { return dosage; }

    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }

    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationDays() { return durationDays; }

    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Integer getQuantity() { return quantity; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getInstructions() { return instructions; }

    public void setInstructions(String instructions) { this.instructions = instructions; }
}
