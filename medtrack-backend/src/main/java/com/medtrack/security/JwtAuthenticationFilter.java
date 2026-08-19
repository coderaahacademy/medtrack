package com.medtrack.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT bearer tokens.
 *
 * For protected requests containing the "Authorization: Bearer <token>" header:
 * 1. Extracts and verifies token signature and expiration.
 * 2. Loads current user details from database to ensure account is still ACTIVE
 *    and roles reflect current state (preventing disabled or deleted users from access).
 * 3. Populates SecurityContextHolder with authenticated principal and current authorities.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MedTrackUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   MedTrackUserDetailsService userDetailsService,
                                   CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If no Bearer authorization header is present, continue filter chain.
        // Spring Security's authorization rules will permit public endpoints or reject unauthenticated requests.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();
        if (jwt.isEmpty()) {
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Bearer token is empty"));
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load current user state from DB to enforce current account status and roles
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Enforce ACTIVE account status (DISABLED accounts cannot authenticate even with a signed token)
                if (!userDetails.isEnabled()) {
                    authenticationEntryPoint.commence(request, response,
                            new DisabledException("User account is disabled"));
                    return;
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    authenticationEntryPoint.commence(request, response,
                            new BadCredentialsException("Invalid JWT token"));
                    return;
                }
            }
        } catch (UsernameNotFoundException ex) {
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("User not found: " + ex.getMessage()));
            return;
        } catch (JwtException | IllegalArgumentException ex) {
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Invalid or expired JWT token: " + ex.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
