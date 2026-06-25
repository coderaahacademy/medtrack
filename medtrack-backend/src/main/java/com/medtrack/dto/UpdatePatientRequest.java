package com.medtrack.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class UpdatePatientRequest implements  PatientRequest{

    @NotBlank(message = "Full name cannot be empty")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    @NotBlank(message = "Gender is required")
    private String gender;
    @NotBlank(message = "Blood group is required")
    @Pattern(
            regexp = "^(A|B|AB|O)[+-]$",
            message = "Invalid blood group. Must be A, B, AB, or O followed by a + or - sign"
    )
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Phone number must be in valid E.164 format (e.g., +12025550143 or +447911123456)"
    )
    private String phone;
    @NotBlank(message = "Address cannot be empty")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    public UpdatePatientRequest() {}

    public String getFullName() {return fullName;}

    public void setFullName(String fullName) {this.fullName = fullName;}

    public LocalDate getBirthDate() {return birthDate;}

    public void setBirthDate(LocalDate birthDate) {this.birthDate = birthDate;}

    public String getGender() {return gender;}

    public void setGender(String gender) {this.gender = gender;}

    public String getBloodGroup() {return bloodGroup;}

    public void setBloodGroup(String bloodGroup) {this.bloodGroup = bloodGroup;}

    public String getAllergies() {return allergies;}

    public void setAllergies(String allergies) {this.allergies = allergies;}

    public String getChronicConditions() {return chronicConditions;}

    public void setChronicConditions(String chronicConditions) {this.chronicConditions = chronicConditions;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}
}
