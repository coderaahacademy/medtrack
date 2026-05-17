package com.medtrack.doctor.dto;

import jakarta.validation.constraints.*;

public class CreateDoctorRequest {
    @NotBlank
    private String fullName;
    @NotBlank
    private String specialization;
    @NotBlank
    private String licenseNumber;
    @NotNull
    private Long userId;
    private String phone;
    public CreateDoctorRequest(){

    };

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
