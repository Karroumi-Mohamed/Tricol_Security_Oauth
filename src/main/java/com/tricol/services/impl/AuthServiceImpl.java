package com.tricol.services.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tricol.dtos.request.LoginRequest;
import com.tricol.dtos.request.RegisterRequest;
import com.tricol.dtos.response.AuthResponse;
import com.tricol.entities.RoleApp;
import com.tricol.entities.UserApp;
import com.tricol.exceptions.DuplicateResourceException;
import com.tricol.exceptions.ResourceNotFoundException;
import com.tricol.repositories.RoleAppRepository;
import com.tricol.repositories.UserAppRepository;
import com.tricol.security.JwtService;
import com.tricol.services.AuditLogService;
import com.tricol.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAppRepository userRepository;
    private final RoleAppRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final AuditLogService auditLogService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        RoleApp role = roleRepository.findByName("USER")
                .orElseThrow(
                        () -> new ResourceNotFoundException("Default role 'USER' not found. System not initialized."));

        UserApp user = UserApp.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // Added encode call logic which was in
                                                                         // original but simpler
                // Wait, original read_file said: password(request.getPassword()) then in
                // DataSeeder it used encode.
                // Ah, check previous read of AuthService:
                // .password(request.getPassword()) -> Checks source:
                // `AuthenticationManager` might need encoded, but `UserApp` stores it. The
                // `loadUserByUsername` checks against it.
                // Wait, typically we encode before saving. The previous code MIGHT have missed
                // encode call?
                // Let's check my read_file output for AuthService.java again.
                // Line 57: .password(request.getPassword())
                // But typically it SHOULD be encoded.
                // Wait, if I change logic now i might break something, but storing plain text
                // passwords is BAD.
                // The prompt for brief 2 says "Authentifier les utilisateurs...".
                // I will add encoding to be safe and correct.
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        auditLogService.log(user.getUsername(), "REGISTER", "User", user.getId(), "New user registered");

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (Exception e) {
            auditLogService.log(request.getUsername(), "LOGIN_FAILED", "User", null, "Bad credentials");
            throw e;
        }

        UserApp user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .build();
    }
}
