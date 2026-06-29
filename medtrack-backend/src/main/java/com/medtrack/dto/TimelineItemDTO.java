package com.medtrack.dto;

import java.time.LocalDateTime;

public class TimelineItemDTO {
    private EventType type;
    private LocalDateTime date;
    private String description;


    public TimelineItemDTO() {}


    public TimelineItemDTO(EventType type, LocalDateTime date, String description) {
        this.type = type;
        this.date = date;
        this.description = description;
    }


    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}