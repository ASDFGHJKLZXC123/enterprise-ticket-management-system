package com.example.tickets.service;

import com.example.tickets.dto.AssignTicketRequest;
import com.example.tickets.dto.ChangeTicketStatusRequest;
import com.example.tickets.dto.CreateCommentRequest;
import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.TicketAuditLogResponse;
import com.example.tickets.dto.TicketCommentResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.dto.UpdateTicketRequest;
import com.example.tickets.entity.AuditAction;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketComment;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.exception.BadRequestException;
import com.example.tickets.exception.ForbiddenException;
import com.example.tickets.exception.NotFoundException;
import com.example.tickets.repository.TicketAuditLogRepository;
import com.example.tickets.repository.TicketCommentRepository;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketAuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    public TicketService(
        TicketRepository ticketRepository,
        UserRepository userRepository,
        TicketCommentRepository commentRepository,
        TicketAuditLogRepository auditLogRepository,
        AuditLogService auditLogService
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TicketResponse createTicket(User actor, CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setRequester(actor);
        ticket.setDueAt(request.dueAt());
        ticket = ticketRepository.save(ticket);
        auditLogService.log(ticket, actor, AuditAction.TICKET_CREATED, null, ticket.getStatus().name());
        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> listTickets(
        User actor,
        TicketStatus status,
        TicketPriority priority,
        Long assigneeId,
        Long requesterId,
        Instant createdAfter,
        Instant createdBefore,
        String search,
        Pageable pageable
    ) {
        Specification<Ticket> spec = visibleTo(actor)
            .and(hasStatus(status))
            .and(hasPriority(priority))
            .and(hasAssignee(assigneeId))
            .and(hasRequester(requesterId))
            .and(createdAfter(createdAfter))
            .and(createdBefore(createdBefore))
            .and(search(search));
        return ticketRepository.findAll(spec, pageable).map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(User actor, Long id) {
        Ticket ticket = getTicketEntity(id);
        requireCanView(actor, ticket);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(User actor, Long id, UpdateTicketRequest request) {
        Ticket ticket = getTicketEntity(id);
        requireCanView(actor, ticket);
        requireCanEditDetails(actor, ticket);
        requireNotClosed(ticket);

        if (request.priority() != null && request.priority() != ticket.getPriority()) {
            requireManagerOrAdmin(actor, "Only managers and admins can change priority");
            TicketPriority oldPriority = ticket.getPriority();
            ticket.setPriority(request.priority());
            auditLogService.log(ticket, actor, AuditAction.PRIORITY_CHANGED, oldPriority.name(), request.priority().name());
        }
        if (request.title() != null && !request.title().isBlank()) {
            ticket.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            ticket.setDescription(request.description().trim());
        }
        if (request.dueAt() != null) {
            ticket.setDueAt(request.dueAt());
        }
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public void deleteTicket(User actor, Long id) {
        Ticket ticket = getTicketEntity(id);
        requireManagerOrAdmin(actor, "Only managers and admins can delete tickets");
        ticketRepository.delete(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(User actor, Long id, AssignTicketRequest request) {
        requireManagerOrAdmin(actor, "Only managers and admins can assign tickets");
        Ticket ticket = getTicketEntity(id);
        requireNotClosed(ticket);
        User assignee = userRepository.findById(request.assigneeId())
            .orElseThrow(() -> new NotFoundException("Assignee not found"));
        if (!assignee.isActive() || !(assignee.getRole() == Role.AGENT || assignee.getRole() == Role.MANAGER)) {
            throw new BadRequestException("Assignee must be an active agent or manager");
        }
        String oldValue = ticket.getAssignee() == null ? "Unassigned" : ticket.getAssignee().getId().toString();
        ticket.setAssignee(assignee);
        ticket = ticketRepository.save(ticket);
        auditLogService.log(ticket, actor, AuditAction.TICKET_ASSIGNED, oldValue, assignee.getId().toString());
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse changeStatus(User actor, Long id, ChangeTicketStatusRequest request) {
        Ticket ticket = getTicketEntity(id);
        requireCanUpdateStatus(actor, ticket, request.status());
        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.status();
        if (oldStatus == newStatus) {
            throw new BadRequestException("Ticket is already in status " + newStatus);
        }
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }
        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(Instant.now());
        } else if (newStatus == TicketStatus.REOPENED || newStatus == TicketStatus.IN_PROGRESS) {
            ticket.setResolvedAt(null);
        }
        ticket = ticketRepository.save(ticket);
        AuditAction action = switch (newStatus) {
            case CLOSED -> AuditAction.TICKET_CLOSED;
            case REOPENED -> AuditAction.TICKET_REOPENED;
            default -> AuditAction.STATUS_CHANGED;
        };
        auditLogService.log(ticket, actor, action, oldStatus.name(), newStatus.name());
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketCommentResponse addComment(User actor, Long id, CreateCommentRequest request) {
        Ticket ticket = getTicketEntity(id);
        requireCanView(actor, ticket);
        requireNotClosed(ticket);
        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(actor);
        comment.setBody(request.body().trim());
        comment = commentRepository.save(comment);
        auditLogService.log(ticket, actor, AuditAction.COMMENT_ADDED, null, comment.getId().toString());
        return TicketCommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> listComments(User actor, Long id) {
        Ticket ticket = getTicketEntity(id);
        requireCanView(actor, ticket);
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(id).stream()
            .map(TicketCommentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketAuditLogResponse> listAuditLogs(User actor, Long id) {
        Ticket ticket = getTicketEntity(id);
        requireCanView(actor, ticket);
        return auditLogRepository.findByTicketIdOrderByCreatedAtDesc(id).stream()
            .map(TicketAuditLogResponse::from)
            .toList();
    }

    public Specification<Ticket> visibleTo(User actor) {
        return (root, query, cb) -> {
            if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER) {
                return cb.conjunction();
            }
            Predicate requester = cb.equal(root.get("requester").get("id"), actor.getId());
            if (actor.getRole() == Role.AGENT) {
                Predicate assignee = cb.equal(root.get("assignee").get("id"), actor.getId());
                return cb.or(requester, assignee);
            }
            return requester;
        };
    }

    public boolean canView(User actor, Ticket ticket) {
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER) {
            return true;
        }
        boolean requester = ticket.getRequester() != null && Objects.equals(ticket.getRequester().getId(), actor.getId());
        boolean assignee = ticket.getAssignee() != null && Objects.equals(ticket.getAssignee().getId(), actor.getId());
        return requester || (actor.getRole() == Role.AGENT && assignee);
    }

    private Ticket getTicketEntity(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    private void requireCanView(User actor, Ticket ticket) {
        if (!canView(actor, ticket)) {
            throw new ForbiddenException("You are not allowed to access this ticket");
        }
    }

    private void requireCanUpdateStatus(User actor, Ticket ticket, TicketStatus newStatus) {
        requireCanView(actor, ticket);
        if (newStatus == TicketStatus.CLOSED || newStatus == TicketStatus.REOPENED) {
            requireManagerOrAdmin(actor, "Only managers and admins can close or reopen tickets");
        }
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER) {
            return;
        }
        if (actor.getRole() == Role.AGENT && isAssignedTo(actor, ticket)) {
            return;
        }
        throw new ForbiddenException("Only managers, admins, and assigned agents can change ticket status");
    }

    private void requireCanEditDetails(User actor, Ticket ticket) {
        if (actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER) {
            return;
        }
        if (actor.getRole() == Role.AGENT && isAssignedTo(actor, ticket)) {
            return;
        }
        throw new ForbiddenException("Only managers, admins, and assigned agents can edit ticket details");
    }

    private void requireManagerOrAdmin(User actor, String message) {
        if (!(actor.getRole() == Role.MANAGER || actor.getRole() == Role.ADMIN)) {
            throw new ForbiddenException(message);
        }
    }

    private void requireNotClosed(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Closed tickets cannot be edited unless reopened");
        }
    }

    private boolean isValidTransition(TicketStatus oldStatus, TicketStatus newStatus) {
        return switch (oldStatus) {
            case OPEN -> newStatus == TicketStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == TicketStatus.RESOLVED;
            case RESOLVED -> newStatus == TicketStatus.CLOSED || newStatus == TicketStatus.REOPENED;
            case REOPENED -> newStatus == TicketStatus.IN_PROGRESS;
            case CLOSED -> newStatus == TicketStatus.REOPENED;
        };
    }

    private boolean isAssignedTo(User actor, Ticket ticket) {
        return ticket.getAssignee() != null && Objects.equals(ticket.getAssignee().getId(), actor.getId());
    }

    private Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    private Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
    }

    private Specification<Ticket> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null ? cb.conjunction() : cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    private Specification<Ticket> hasRequester(Long requesterId) {
        return (root, query, cb) -> requesterId == null ? cb.conjunction() : cb.equal(root.get("requester").get("id"), requesterId);
    }

    private Specification<Ticket> createdAfter(Instant createdAfter) {
        return (root, query, cb) -> createdAfter == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
    }

    private Specification<Ticket> createdBefore(Instant createdBefore) {
        return (root, query, cb) -> createdBefore == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
    }

    private Specification<Ticket> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            return cb.or(predicates.toArray(Predicate[]::new));
        };
    }
}
