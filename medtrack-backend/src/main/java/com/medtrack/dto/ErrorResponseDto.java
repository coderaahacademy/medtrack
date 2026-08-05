package com.medtrack.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponseDto {
    private String timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private List<FieldErrorDto> fieldErrors = new ArrayList<>();

    public ErrorResponseDto() {
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponseDto(int status, String error, String code, String message, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public ErrorResponseDto(int status, String error, String code, String message, String path, List<FieldErrorDto> fieldErrors) {
        this(status, error, code, message, path);
        if (fieldErrors != null) {
            this.fieldErrors = fieldErrors;
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldErrorDto> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldErrorDto> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}