package com.example.tickets.service;

import com.example.tickets.dto.CreateUserRequest;
import com.example.tickets.dto.UpdateUserRequest;
import com.example.tickets.dto.UserResponse;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.User;
import com.example.tickets.exception.BadRequestException;
import com.example.tickets.exception.NotFoundException;
import com.example.tickets.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        user.setActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivate(Long id) {
        User user = getUser(id);
        user.setActive(false);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse reactivate(Long id) {
        User user = getUser(id);
        user.setActive(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAssignableUsers() {
        return userRepository.findAll().stream()
            .filter(User::isActive)
            .filter(user -> user.getRole() == Role.AGENT || user.getRole() == Role.MANAGER)
            .map(UserResponse::from)
            .toList();
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
