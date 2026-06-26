package com.medtrack.dto;

import java.time.LocalDateTime;

public class TimelineItemDTO {
    private String type;
    private LocalDateTime date;
    private String title;
    private String description;

    public TimelineItemDTO() {}

    public TimelineItemDTO(String type, LocalDateTime date, String title, String description) {
        this.type = type;
        this.date = date;
        this.title = title;
        this.description = description;
    }

    public String getType() { return type; }
    public LocalDateTime getDate() { return date; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}