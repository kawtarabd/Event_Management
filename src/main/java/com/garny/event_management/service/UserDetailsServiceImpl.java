package com.garny.event_management.service;

import com.garny.event_management.entity.User;
import org.springframework.security.core.Authentication; // Add this import

import com.garny.event_management.repository.UserRepository;
import com.garny.event_management.security.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return new CustomUserDetails(user);
    }

    // New method to find a user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}