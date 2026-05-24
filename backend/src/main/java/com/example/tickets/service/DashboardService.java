package com.example.tickets.service;

import com.example.tickets.dto.CountResponse;
import com.example.tickets.dto.DashboardSummaryResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.repository.TicketRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    public DashboardService(TicketRepository ticketRepository, TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(User actor) {
        Specification<Ticket> visible = ticketService.visibleTo(actor);
        List<TicketResponse> recent = ticketRepository
            .findAll(visible, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
            .map(TicketResponse::from)
            .toList();

        return new DashboardSummaryResponse(
            ticketRepository.count(visible),
            countByStatus(visible, TicketStatus.OPEN),
            countByStatus(visible, TicketStatus.IN_PROGRESS),
            countByStatus(visible, TicketStatus.RESOLVED),
            countByStatus(visible, TicketStatus.CLOSED),
            ticketRepository.count(visible.and((root, query, cb) -> root.get("priority").in(TicketPriority.HIGH, TicketPriority.URGENT))),
            ticketRepository.count(visible.and((root, query, cb) -> cb.and(
                cb.lessThan(root.get("dueAt"), Instant.now()),
                cb.not(root.get("status").in(TicketStatus.RESOLVED, TicketStatus.CLOSED))
            ))),
            recent
        );
    }

    @Transactional(readOnly = true)
    public List<CountResponse> ticketsByStatus(User actor) {
        Specification<Ticket> visible = ticketService.visibleTo(actor);
        return Arrays.stream(TicketStatus.values())
            .map(status -> new CountResponse(status.name(), countByStatus(visible, status)))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CountResponse> ticketsByPriority(User actor) {
        Specification<Ticket> visible = ticketService.visibleTo(actor);
        return Arrays.stream(TicketPriority.values())
            .map(priority -> new CountResponse(priority.name(), ticketRepository.count(visible.and((root, query, cb) -> cb.equal(root.get("priority"), priority)))))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CountResponse> ticketsByAssignee(User actor) {
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER) {
            return ticketRepository.countByAssigneeGroup().stream()
                .map(row -> new CountResponse(row.getKey(), row.getCount()))
                .toList();
        }
        return ticketRepository.findAll(ticketService.visibleTo(actor)).stream()
            .collect(java.util.stream.Collectors.groupingBy(
                ticket -> ticket.getAssignee() == null ? "Unassigned" : ticket.getAssignee().getFullName(),
                java.util.stream.Collectors.counting()
            ))
            .entrySet()
            .stream()
            .map(entry -> new CountResponse(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(CountResponse::count).reversed())
            .toList();
    }

    private long countByStatus(Specification<Ticket> visible, TicketStatus status) {
        Objects.requireNonNull(status, "status");
        return ticketRepository.count(visible.and((root, query, cb) -> cb.equal(root.get("status"), status)));
    }
}

