package com.example.tickets.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
    @NotNull Long assigneeId
) {
}

