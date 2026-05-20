package com.medtrack.patient.api;

import java.time.LocalDate;

public class PatientRequest {

    private Long userId;
    private Long familyDoctorId;
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
    private String phone;
    private String address;

    public PatientRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public Long getFamilyDoctorId() {
        return familyDoctorId;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFamilyDoctorId(Long familyDoctorId) {
        this.familyDoctorId = familyDoctorId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public void setChronicConditions(String chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}