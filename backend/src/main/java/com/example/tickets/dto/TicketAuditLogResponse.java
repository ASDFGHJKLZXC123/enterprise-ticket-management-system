package com.example.tickets.dto;

import com.example.tickets.entity.AuditAction;
import com.example.tickets.entity.TicketAuditLog;
import java.time.Instant;

public record TicketAuditLogResponse(
    Long id,
    Long ticketId,
    UserResponse actor,
    AuditAction action,
    String oldValue,
    String newValue,
    Instant createdAt
) {
    public static TicketAuditLogResponse from(TicketAuditLog auditLog) {
        return new TicketAuditLogResponse(
            auditLog.getId(),
            auditLog.getTicket().getId(),
            UserResponse.from(auditLog.getActor()),
            auditLog.getAction(),
            auditLog.getOldValue(),
            auditLog.getNewValue(),
            auditLog.getCreatedAt()
        );
    }
}

