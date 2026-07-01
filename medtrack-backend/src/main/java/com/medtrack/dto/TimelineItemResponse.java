package com.medtrack.dto;

import java.time.LocalDateTime;

public class TimelineItemResponse {
    private String type;
    private LocalDateTime date;
    private String title;
    private String description;

    public TimelineItemResponse() {
    }

    public TimelineItemResponse(String type, LocalDateTime date, String title, String description) {
        this.type = type;
        this.date = date;
        this.title = title;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}