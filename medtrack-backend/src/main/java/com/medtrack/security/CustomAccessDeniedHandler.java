package com.medtrack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtrack.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * AccessDeniedHandler invoked when an authenticated user attempts to access
 * a protected resource without having the necessary authorities or permissions.
 *
 * Spring Security filters execute before Spring MVC DispatcherServlet and @RestControllerAdvice.
 * When authorization fails at the filter level or is forwarded to the security exception handling chain,
 * this handler writes the standardized T41 ErrorResponseDto format directly.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message = accessDeniedException != null && accessDeniedException.getMessage() != null
                && !accessDeniedException.getMessage().equalsIgnoreCase("Access is denied")
                && !accessDeniedException.getMessage().equalsIgnoreCase("Access Denied")
                ? accessDeniedException.getMessage()
                : "You do not have permission to access this resource";

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "FORBIDDEN",
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                Collections.emptyList()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
