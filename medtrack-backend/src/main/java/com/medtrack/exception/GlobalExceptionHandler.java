package com.medtrack.exception;

import com.medtrack.dto.ErrorResponseDto;
import com.medtrack.dto.FieldErrorDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank() || "Bad credentials".equals(message)) {
            message = "Invalid email or password";
        }
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                message,
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()
                || "Access is denied".equalsIgnoreCase(message)
                || "Access Denied".equalsIgnoreCase(message)) {
            message = "You do not have permission to access this resource";
        }
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                message,
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = switch (status) {
            case NOT_FOUND -> "RESOURCE_NOT_FOUND";
            case CONFLICT -> "CONFLICT";
            case BAD_REQUEST -> "BAD_REQUEST";
            case FORBIDDEN -> "FORBIDDEN";
            case UNPROCESSABLE_ENTITY -> "UNPROCESSABLE_ENTITY";
            default -> status.name();
        };

        return buildResponse(
                status,
                code,
                ex.getReason(),
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTransition(
            InvalidStatusTransitionException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "INVALID_STATE_TRANSITION",
                ex.getMessage(),
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(InvalidCancellationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCancellation(
            InvalidCancellationException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_CANCELLATION",
                ex.getMessage(),
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "ILLEGAL_ARGUMENT",
                ex.getMessage() != null ? ex.getMessage() : "Bad request",
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDto> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.add(new FieldErrorDto(fieldName, errorMessage));
        });

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedInputException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_INPUT",
                "Required request body is missing or invalid JSON format",
                request,
                Collections.emptyList()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = "Invalid value for parameter '" + ex.getName() + "'";
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            message += ". Allowed values: " + java.util.Arrays.toString(requiredType.getEnumConstants());
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                message,
                request,
                List.of(new FieldErrorDto(ex.getName(),
                        ex.getValue() != null ? "Invalid value: " + ex.getValue() : "Invalid value"))
        );
    }

    /**
     * Sorting by a property the entity does not have is a client mistake, not a server fault.
     * Spring Data raises this while resolving a Pageable's sort, e.g. ?sort=doesNotExist,asc.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponseDto> handleUnknownSortProperty(
            PropertyReferenceException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_SORT_PROPERTY",
                "Unknown sort property '" + ex.getPropertyName() + "'",
                request,
                List.of(new FieldErrorDto("sort", "Unknown property: " + ex.getPropertyName()))
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request,
                Collections.emptyList()
        );
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<FieldErrorDto> fieldErrors) {

        ErrorResponseDto response = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}