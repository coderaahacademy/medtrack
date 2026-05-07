package com.medtrack.doctor.domain;

import com.medtrack.user.domain.User;
import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String specialization;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Doctor() {
    }

    public Doctor(String fullName, String specialization, User user) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.user = user;
    }

    public String getFullname() {
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

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user=user;
    }

    public Long getId() {
        return id;
    }
}
