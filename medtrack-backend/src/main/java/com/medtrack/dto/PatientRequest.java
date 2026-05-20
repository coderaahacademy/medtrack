package com.medtrack.dto;
import java.time.LocalDate;
public class PatientRequest {
    private Long userId; private Long familyDoctorId; private String fullName; private LocalDate birthDate; private String gender; private String bloodGroup; private String allergies; private String chronicConditions; private String phone; private String address;
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Long getFamilyDoctorId() { return familyDoctorId; } public void setFamilyDoctorId(Long familyDoctorId) { this.familyDoctorId = familyDoctorId; }
    public String getFullName() { return fullName; } public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getBirthDate() { return birthDate; } public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; } public void setGender(String gender) { this.gender = gender; }
    public String getBloodGroup() { return bloodGroup; } public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getAllergies() { return allergies; } public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getChronicConditions() { return chronicConditions; } public void setChronicConditions(String chronicConditions) { this.chronicConditions = chronicConditions; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
}
