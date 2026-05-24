package com.example.tickets.dto;

import com.example.tickets.entity.TicketPriority;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateTicketRequest(
    @Size(max = 200) String title,
    String description,
    TicketPriority priority,
    Instant dueAt
) {
}

