package com.example.tickets.dto;

import com.example.tickets.entity.TicketComment;
import java.time.Instant;

public record TicketCommentResponse(
    Long id,
    Long ticketId,
    UserResponse author,
    String body,
    Instant createdAt
) {
    public static TicketCommentResponse from(TicketComment comment) {
        return new TicketCommentResponse(
            comment.getId(),
            comment.getTicket().getId(),
            UserResponse.from(comment.getAuthor()),
            comment.getBody(),
            comment.getCreatedAt()
        );
    }
}

