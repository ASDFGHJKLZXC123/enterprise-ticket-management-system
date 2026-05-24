package com.example.tickets.service;

import com.example.tickets.dto.LoginRequest;
import com.example.tickets.dto.LoginResponse;
import com.example.tickets.dto.RegisterRequest;
import com.example.tickets.dto.UserResponse;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.User;
import com.example.tickets.exception.BadRequestException;
import com.example.tickets.repository.UserRepository;
import com.example.tickets.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.REQUESTER);
        user.setActive(true);
        user = userRepository.save(user);
        return new LoginResponse(jwtTokenProvider.createToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return new LoginResponse(jwtTokenProvider.createToken(user), UserResponse.from(user));
    }
}

