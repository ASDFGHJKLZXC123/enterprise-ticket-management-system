package com.example.tickets.dto;

import com.example.tickets.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTicketStatusRequest(
    @NotNull TicketStatus status
) {
}

