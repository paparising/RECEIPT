package com.example.receipt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.receipt.dto.JwtResponse;
import com.example.receipt.dto.LoginRequest;
import com.example.receipt.dto.SignupRequest;
import com.example.receipt.entity.Role;
import com.example.receipt.entity.User;
import com.example.receipt.repository.RoleRepository;
import com.example.receipt.repository.UserRepository;
import com.example.receipt.security.JwtTokenProvider;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtResponse login(LoginRequest loginRequest) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String jwt = tokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
    }

    public String signup(SignupRequest signupRequest) throws Exception {
        // Check if username exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new Exception("Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new Exception("Email is already in use!");
        }

        // Create new user account
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEnabled(true);

        // Assign default role (USER)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new Exception("Role USER not found"));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        return "User registered successfully!";
    }
}
