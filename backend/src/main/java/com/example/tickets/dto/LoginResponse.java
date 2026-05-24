package com.example.tickets.dto;

public record LoginResponse(
    String token,
    UserResponse user
) {
}

