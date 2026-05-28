package com.medtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateDoctorRequest {

    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    @NotBlank(message = "Specialization is required")
    private String specialization;
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Phone number must be in valid E.164 format (e.g., +12025550143 or +447911123456)"
    )
    private String phone;
    private boolean active;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
