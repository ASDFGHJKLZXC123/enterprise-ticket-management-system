package com.example.tickets.dto;

import com.example.tickets.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 120) String password,
    @NotBlank @Size(max = 120) String fullName,
    @NotNull Role role
) {
}

