package com.medtrack.dto;

import java.time.LocalDateTime;

public class MedicationResponse {

    private Long id;
    private String name;
    private String genericName;
    private String brand;
    private String dosageForm;
    private String strength;
    private boolean requiresPrescription;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public  MedicationResponse() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

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

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
