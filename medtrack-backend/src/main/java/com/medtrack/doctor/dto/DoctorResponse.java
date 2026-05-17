package com.medtrack.doctor.dto;

import com.medtrack.doctor.entity.Doctor;

public class DoctorResponse {
    private Long id;
    private String fullName;
    private String specialization;
    private String licenseNumber;
    private String phone;
    private boolean active;
    private Long userId;

    public DoctorResponse(){

    }

    public DoctorResponse(Long id, String fullName, String specialization,String licenseNumber,String phone,boolean active,Long userId){
        this.id=id;
        this.fullName=fullName;
        this.specialization=specialization;
        this.licenseNumber=licenseNumber;
        this.phone=phone;
        this.active=active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static DoctorResponse mapToResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setFullName(doctor.getFullName());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setPhone(doctor.getPhone());
        response.setActive(doctor.isActive());
        if (doctor.getUser() != null) {
            response.setUserId(doctor.getUser().getId());
        }
        return response;
    }
}
