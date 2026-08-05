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

    public ErrorResponseDto(int status, String error, String code, String message, String path) {
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = LocalDateTime.now().toString();
    }

    public ErrorResponseDto(int status, String error, String code, String message, String path, List<FieldErrorDto> fieldErrors) {
        this(status, error, code, message, path);
        this.fieldErrors = fieldErrors;
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldErrorDto> getFieldErrors() { return fieldErrors; }
}