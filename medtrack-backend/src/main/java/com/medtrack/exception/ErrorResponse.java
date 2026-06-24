package com.medtrack.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Map<String, String> errors;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, int status, String message, Map<String, String> errors, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.path = path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, String> getErrors() { return errors; }
    public String getPath() { return path; }
}