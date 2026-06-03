package com.medtrack.dto;

import jakarta.validation.constraints.NotBlank;

public class NotesRequest {

    @NotBlank(message = "Notes content cannot be empty")
    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
