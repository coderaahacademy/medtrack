package com.medtrack.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for MedTrack REST API.
 * Configures stateless JWT authentication, public registration/login endpoints,
 * OpenAPI/Swagger access, and standardized 401 error handling.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is disabled because this REST API is completely stateless and uses
                // Bearer JWT tokens in the HTTP Authorization header. Because browsers do not automatically
                // attach authentication credentials (unlike session cookies), the application is not vulnerable
                // to Cross-Site Request Forgery (CSRF).
                .csrf(csrf -> csrf.disable())
                // Ensure Spring Security does not create or use HttpSession instances for security contexts.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Disable unused legacy authentication mechanisms.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // Standardized 401 error responses for authentication failures.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public authentication endpoints for user registration and login
                        .requestMatchers(HttpMethod.POST, "/users/register", "/users/login").permitAll()
                        // Public API documentation (Swagger UI and OpenAPI specs)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Development-only H2 console access
                        .requestMatchers("/h2-console/**").permitAll()
                        // All other business API endpoints require an authenticated user
                        .anyRequest().authenticated()
                )
                // Allow iframe rendering for the H2 database web console during development
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                // Execute the JWT filter before the standard UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
