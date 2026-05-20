package com.medtrack.patient.api;

import com.medtrack.patient.domain.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientResponse {

    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PatientResponse() {
    }

    public PatientResponse(
            Long id,
            Long userId,
            Long familyDoctorId,
            String fullName,
            LocalDate birthDate,
            String gender,
            String bloodGroup,
            String allergies,
            String chronicConditions,
            String phone,
            String address,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.familyDoctorId = familyDoctorId;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.chronicConditions = chronicConditions;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PatientResponse fromPatient(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getUser().getId(),
                patient.getFamilyDoctorId(),
                patient.getFullName(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getAllergies(),
                patient.getChronicConditions(),
                patient.getPhone(),
                patient.getAddress(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}