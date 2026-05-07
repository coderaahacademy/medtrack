package com.medtrack.doctor.api;

import com.medtrack.doctor.domain.Doctor;

public class DoctorResponse {
    private Long id;
    private String fullName;
    private String specialization;
    private Long userId;

    public DoctorResponse(){

    }

    public DoctorResponse(Long id, String fullName, String specialization,Long userId){
        this.id=id;
        this.fullName=fullName;
        this.specialization=specialization;
        this.userId=userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public static DoctorResponse mapToResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setFullName(doctor.getFullname());
        response.setSpecialization(doctor.getSpecialization());
        if (doctor.getUser() != null) {
            response.setUserId(doctor.getUser().getId());
        }
        return response;
    }
}
