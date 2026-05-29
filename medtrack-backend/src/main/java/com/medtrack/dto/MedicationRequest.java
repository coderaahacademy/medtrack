package com.medtrack.dto;

import jakarta.validation.constraints.NotBlank;

public class MedicationRequest {

    @NotBlank(message = "Medication name is required")
    private String name;
    @NotBlank(message = "Generic name is required")
    private String genericName;
    private String brand;
    @NotBlank(message = "Dosage form is required (e.g., Tablet, Syrup, Injection)")
    private String dosageForm;
    @NotBlank(message = "Strength is required (e.g., 500mg, 10mg/mL)")
    private String strength;
    private boolean requiresPrescription;
    private boolean active;

    public MedicationRequest() {}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getGenericName() {return genericName;}

    public void setGenericName(String genericName) {this.genericName = genericName;}

    public String getBrand() {return brand;}

    public void setBrand(String brand) {this.brand = brand;}

    public String getDosageForm() {return dosageForm;}

    public void setDosageForm(String dosageForm) {this.dosageForm = dosageForm;}

    public String getStrength() {return strength;}

    public void setStrength(String strength) {this.strength = strength;}

    public boolean isRequiresPrescription() {return requiresPrescription;}

    public void setRequiresPrescription(
            boolean requiresPrescription) {this.requiresPrescription = requiresPrescription;}

    public boolean isActive() {return active;}

    public void setActive(boolean active) {this.active = active;}
}
