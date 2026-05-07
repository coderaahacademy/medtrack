package com.medtrack.doctor.api;

public class UpdateDoctorRequest {
    private String fullname;
    private String specialization;

    public UpdateDoctorRequest() {

    }

    public String getFullName() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
