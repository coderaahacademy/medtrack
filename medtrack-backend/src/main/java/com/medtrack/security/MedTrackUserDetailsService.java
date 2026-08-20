package com.medtrack.security;

import com.medtrack.entity.User;
import com.medtrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for MedTrack.
 * Loads user details from the database and adapts them into MedTrackUserDetails.
 */
@Service
public class MedTrackUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MedTrackUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new MedTrackUserDetails(user);
    }
}
