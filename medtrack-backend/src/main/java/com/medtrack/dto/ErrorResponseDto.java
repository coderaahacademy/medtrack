package com.medtrack.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponseDto {
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldErrorDto> fieldErrors;


    public ErrorResponseDto(int status, String error, String code, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public ErrorResponseDto(int status, String error, String code, String message, String path, LocalDateTime timestamp, List<FieldErrorDto> fieldErrors) {
        this(status, error, code, message, path, timestamp);
        this.fieldErrors = fieldErrors;
    }

    // Getters
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldErrorDto> getFieldErrors() { return fieldErrors; }
}