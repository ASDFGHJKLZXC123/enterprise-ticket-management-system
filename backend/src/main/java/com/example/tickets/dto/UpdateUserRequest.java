package com.example.tickets.dto;

import com.example.tickets.entity.Role;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(max = 120) String fullName,
    Role role,
    Boolean active
) {
}

