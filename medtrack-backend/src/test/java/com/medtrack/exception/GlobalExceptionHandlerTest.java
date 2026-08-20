package com.medtrack.exception;

import com.medtrack.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
    }

    // Used via reflection to build a real MethodArgumentNotValidException below.
    private void dummyMethodForTest(String value) {}

    @Test
    void shouldReturn404ForResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient not found with ID: 42");

        ResponseEntity<ErrorResponseDto> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("RESOURCE_NOT_FOUND", body.getCode());
        assertEquals("Patient not found with ID: 42", body.getMessage());
        assertEquals("/api/test", body.getPath());
        assertTrue(body.getFieldErrors().isEmpty());
    }

    @Test
    void shouldReturn404ForResponseStatusExceptionNotFound() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with ID: 7");

        ResponseEntity<ErrorResponseDto> response = handler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
        assertEquals("Doctor not found with ID: 7", response.getBody().getMessage());
    }

    @Test
    void shouldReturn409ForResponseStatusExceptionConflict() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "Patient profile already exists");

        ResponseEntity<ErrorResponseDto> response = handler.handleResponseStatusException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getCode());
    }

    @Test
    void shouldReturn409ForInvalidStatusTransition() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("Cannot move from COMPLETED to ISSUED");

        ResponseEntity<ErrorResponseDto> response = handler.handleInvalidTransition(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("INVALID_STATE_TRANSITION", response.getBody().getCode());
        assertEquals("Cannot move from COMPLETED to ISSUED", response.getBody().getMessage());
    }

    @Test
    void shouldReturn400WithMultipleFieldErrorsForValidationFailure() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("dummyMethodForTest", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "patient");
        bindingResult.addError(new FieldError("patient", "fullName", "Full name cannot be empty"));
        bindingResult.addError(new FieldError("patient", "phone", "Phone number is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponseDto> response = handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertEquals("VALIDATION_FAILED", body.getCode());
        assertEquals(2, body.getFieldErrors().size());
        assertTrue(body.getFieldErrors().stream().anyMatch(fe -> fe.getField().equals("fullName")));
        assertTrue(body.getFieldErrors().stream().anyMatch(fe -> fe.getField().equals("phone")));
    }

    @Test
    void shouldReturn400ForMalformedInput() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Required request body is missing or invalid JSON format",
                (org.springframework.http.HttpInputMessage) null
        );

        ResponseEntity<ErrorResponseDto> response = handler.handleMalformedInputException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("MALFORMED_INPUT", response.getBody().getCode());
        assertTrue(response.getBody().getFieldErrors().isEmpty());
    }

    @Test
    void shouldReturn400ForIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid enum value provided");

        ResponseEntity<ErrorResponseDto> response = handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ILLEGAL_ARGUMENT", response.getBody().getCode());
        assertEquals("Invalid enum value provided", response.getBody().getMessage());
    }

    @Test
    void shouldReturn401ForAuthenticationException() {
        org.springframework.security.authentication.BadCredentialsException ex =
                new org.springframework.security.authentication.BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponseDto> response = handler.handleAuthenticationException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(401, body.getStatus());
        assertEquals("UNAUTHORIZED", body.getCode());
        assertEquals("Invalid email or password", body.getMessage());
        assertEquals("/api/test", body.getPath());
        assertTrue(body.getFieldErrors().isEmpty());
    }

    @Test
    void shouldReturn500ForUnexpectedException() {
        RuntimeException ex = new RuntimeException("Something broke unexpectedly");

        ResponseEntity<ErrorResponseDto> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponseDto body = response.getBody();
        assertEquals("INTERNAL_SERVER_ERROR", body.getCode());
        assertEquals("An unexpected error occurred", body.getMessage());
        assertTrue(body.getFieldErrors().isEmpty());
    }
}