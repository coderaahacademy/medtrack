package com.medtrack.dto;

public class MessageResponse {
    String  message;
    public MessageResponse(){}
    public MessageResponse(String message){ this.message = message; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
