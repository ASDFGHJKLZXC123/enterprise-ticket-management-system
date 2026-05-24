package com.example.tickets.service;

import com.example.tickets.entity.AuditAction;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketAuditLog;
import com.example.tickets.entity.User;
import com.example.tickets.repository.TicketAuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final TicketAuditLogRepository auditLogRepository;

    public AuditLogService(TicketAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public TicketAuditLog log(Ticket ticket, User actor, AuditAction action, String oldValue, String newValue) {
        TicketAuditLog auditLog = new TicketAuditLog();
        auditLog.setTicket(ticket);
        auditLog.setActor(actor);
        auditLog.setAction(action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        return auditLogRepository.save(auditLog);
    }
}

