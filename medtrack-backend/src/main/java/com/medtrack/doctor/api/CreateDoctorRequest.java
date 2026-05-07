package com.medtrack.doctor.api;

//import javax.validation.constrains.*;

public class CreateDoctorRequest {
    //NotBlank
    private String fullName;
    //@NotBlank
    private String specialization;
    //@NotNulL
    private Long userId;
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
}
