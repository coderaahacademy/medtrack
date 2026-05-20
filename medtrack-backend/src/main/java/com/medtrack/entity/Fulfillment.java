package com.medtrack.entity;

import jakarta.persistence.*;

@Entity
public class Fulfillment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prescriptionDetails;
    private String status;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPrescriptionDetails() { return prescriptionDetails; }
    public void setPrescriptionDetails(String prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}