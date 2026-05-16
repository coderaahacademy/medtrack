package com.medtrack.prescription.dto;

public class PrescriptionItemRequest {

    private Long medicationId;
    private String dosage;
    private String frequency;
    private Integer durationDays;
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
