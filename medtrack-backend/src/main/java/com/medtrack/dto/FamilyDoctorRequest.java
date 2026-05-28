package com.medtrack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FamilyDoctorRequest {
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be a positive number")
    private Long familyDoctorId;

    public Long getFamilyDoctorId() {return familyDoctorId;}

    public void setFamilyDoctorId(Long familyDoctorId) {this.familyDoctorId = familyDoctorId;}

    public FamilyDoctorRequest() {}
}
