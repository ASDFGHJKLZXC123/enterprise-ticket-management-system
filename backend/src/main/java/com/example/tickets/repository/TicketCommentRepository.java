package com.example.tickets.repository;

import com.example.tickets.entity.TicketComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

