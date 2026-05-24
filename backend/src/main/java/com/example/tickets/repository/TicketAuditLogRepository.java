package com.example.tickets.repository;

import com.example.tickets.entity.TicketAuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAuditLogRepository extends JpaRepository<TicketAuditLog, Long> {
    List<TicketAuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}

