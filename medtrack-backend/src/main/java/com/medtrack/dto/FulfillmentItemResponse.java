package com.medtrack.dto;

/**
 * T46: One prescription item as the pharmacy needs to see it while dispensing.
 */
public class FulfillmentItemResponse {

    private Long prescriptionItemId;
    private Long medicationId;
    private String medicationName;
    private String strength;
    private String dosageForm;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;

    private Integer prescribedQuantity;
    private Integer dispensedQuantity;
    private Integer remainingQuantity;

    public FulfillmentItemResponse() {
    }

    public Long getPrescriptionItemId() { return prescriptionItemId; }
    public void setPrescriptionItemId(Long prescriptionItemId) { this.prescriptionItemId = prescriptionItemId; }

    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getPrescribedQuantity() { return prescribedQuantity; }
    public void setPrescribedQuantity(Integer prescribedQuantity) { this.prescribedQuantity = prescribedQuantity; }

    public Integer getDispensedQuantity() { return dispensedQuantity; }
    public void setDispensedQuantity(Integer dispensedQuantity) { this.dispensedQuantity = dispensedQuantity; }

    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }
}
