package com.medtrack.dto;

import java.time.LocalDateTime;

public class VisitResponse {
  private Long id;
  private Long patientId;
  private Long doctorId;
  private LocalDateTime visitDate;
  private String symptoms;
  private String diagnosis;
  private String notes;
  private LocalDateTime followUpDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public VisitResponse() {}

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public Long getPatientId() { return patientId; }

  public void setPatientId(Long patientId) { this.patientId = patientId; }

  public Long getDoctorId() { return doctorId; }

  public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

  public LocalDateTime getVisitDate() { return visitDate; }

  public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

  public String getSymptoms() { return symptoms; }

  public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

  public String getDiagnosis() { return diagnosis; }

  public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

  public String getNotes() { return notes; }

  public void setNotes(String notes) { this.notes = notes; }

  public LocalDateTime getFollowUpDate() { return followUpDate; }

  public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }

  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
