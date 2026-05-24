package com.example.tickets.dto;

import com.example.tickets.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateTicketRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String description,
    @NotNull TicketPriority priority,
    Instant dueAt
) {
}

