package com.medtrack.dto;

public interface DoctorRequest {
    String getFullName();
    String getSpecialization();
    String getLicenseNumber();
    String getPhone();
    boolean isActive();
}
