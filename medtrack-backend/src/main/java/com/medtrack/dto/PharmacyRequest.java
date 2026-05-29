package com.medtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PharmacyRequest {

    @NotBlank(message = "Pharmacy name is required")
    private String name;
    @NotBlank(message = "Address cannot be empty")
    private String address;
    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Phone number must be in valid E.164 format (e.g., +12025550143 or +447911123456)"
    )
    private String phone;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private boolean active;

    public PharmacyRequest() {}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public String getPhone() {return phone;}

    public void setPhone(String phone) {this.phone = phone;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public boolean isActive() {return active;}

    public void setActive(boolean active) {this.active = active;}
}
