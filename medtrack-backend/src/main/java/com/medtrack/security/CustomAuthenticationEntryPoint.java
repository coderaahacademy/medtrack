package com.medtrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrack.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * AuthenticationEntryPoint invoked when an unauthenticated request attempts to access
 * a protected resource or when authentication fails within the Spring Security filter chain.
 *
 * Spring Security filters execute before Spring MVC DispatcherServlet and @RestControllerAdvice,
 * so authentication errors originating in the security filter chain cannot be caught by
 * GlobalExceptionHandler. This entry point writes the standardized T41 ErrorResponseDto format directly.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message = (String) request.getAttribute("auth_error_message");
        if (message == null || message.isBlank()) {
            message = authException != null && authException.getMessage() != null
                    ? authException.getMessage()
                    : "Unauthorized";
        }

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "UNAUTHORIZED",
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
