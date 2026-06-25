package com.medtrack.dto;

import java.time.LocalDate;

public interface PatientRequest {
    String getFullName();
    LocalDate getBirthDate();
    String getGender();
    String getBloodGroup();
    String getAllergies();
    String getChronicConditions();
    String getPhone();
    String getAddress();
}
