package com.example.tickets.dto;

import com.example.tickets.entity.Role;
import com.example.tickets.entity.User;
import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    Role role,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

