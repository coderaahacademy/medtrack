package com.medtrack.security;

import com.medtrack.entity.User;
import com.medtrack.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dedicated Spring Security UserDetails adapter wrapping the MedTrack User JPA entity.
 * Keeps security concerns separate from the persistence entity while providing all
 * necessary authentication and authorization metadata to Spring Security.
 */
public class MedTrackUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public MedTrackUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        // A user is only enabled in Spring Security if their status is ACTIVE.
        // A DISABLED account will be rejected during authentication.
        this.enabled = user.getStatus() == UserStatus.ACTIVE;

        // Map all UserRole entries to Spring Security authorities with the ROLE_ prefix.
        // Support multiple roles per user as designed in the MedTrack data model.
        if (user.getRoles() != null) {
            this.authorities = user.getRoles().stream()
                    .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().name()))
                    .collect(Collectors.toList());
        } else {
            this.authorities = Collections.emptyList();
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
