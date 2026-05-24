package com.example.tickets.dto;

import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import java.time.Instant;

public record TicketResponse(
    Long id,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    UserResponse requester,
    UserResponse assignee,
    Instant dueAt,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getStatus(),
            ticket.getPriority(),
            UserResponse.from(ticket.getRequester()),
            UserResponse.from(ticket.getAssignee()),
            ticket.getDueAt(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getResolvedAt()
        );
    }
}

