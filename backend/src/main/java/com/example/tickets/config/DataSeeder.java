package com.example.tickets.config;

import com.example.tickets.entity.AuditAction;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketComment;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.repository.TicketCommentRepository;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.repository.UserRepository;
import com.example.tickets.service.AuditLogService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
        UserRepository userRepository,
        TicketRepository ticketRepository,
        TicketCommentRepository commentRepository,
        AuditLogService auditLogService,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = user("admin@example.com", "Platform Admin", Role.ADMIN);
        User manager = user("manager@example.com", "Service Manager", Role.MANAGER);
        User agent = user("agent@example.com", "Support Agent", Role.AGENT);
        User requester = user("requester@example.com", "Requesting User", Role.REQUESTER);
        userRepository.saveAll(List.of(admin, manager, agent, requester));

        Ticket open = ticket("Unable to access dashboard", "The dashboard returns an error after login.", TicketStatus.OPEN, TicketPriority.HIGH, requester, null, Instant.now().plus(3, ChronoUnit.DAYS));
        Ticket assigned = ticket("VPN certificate expired", "Remote access fails with certificate validation errors.", TicketStatus.IN_PROGRESS, TicketPriority.URGENT, requester, agent, Instant.now().plus(1, ChronoUnit.DAYS));
        Ticket overdue = ticket("Payroll report missing", "Monthly payroll report did not generate.", TicketStatus.OPEN, TicketPriority.HIGH, requester, null, Instant.now().minus(2, ChronoUnit.DAYS));
        Ticket resolved = ticket("Printer queue stalled", "Finance printer jobs are stuck.", TicketStatus.RESOLVED, TicketPriority.MEDIUM, requester, agent, Instant.now().minus(1, ChronoUnit.DAYS));
        resolved.setResolvedAt(Instant.now().minus(4, ChronoUnit.HOURS));
        Ticket closed = ticket("Badge reader reboot", "Lobby badge reader stopped responding.", TicketStatus.CLOSED, TicketPriority.LOW, requester, agent, Instant.now().minus(5, ChronoUnit.DAYS));
        closed.setResolvedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        ticketRepository.saveAll(List.of(open, assigned, overdue, resolved, closed));

        for (Ticket ticket : List.of(open, assigned, overdue, resolved, closed)) {
            auditLogService.log(ticket, requester, AuditAction.TICKET_CREATED, null, ticket.getStatus().name());
            if (ticket.getAssignee() != null) {
                auditLogService.log(ticket, manager, AuditAction.TICKET_ASSIGNED, "Unassigned", ticket.getAssignee().getId().toString());
            }
        }
        auditLogService.log(assigned, agent, AuditAction.STATUS_CHANGED, TicketStatus.OPEN.name(), TicketStatus.IN_PROGRESS.name());
        auditLogService.log(resolved, agent, AuditAction.STATUS_CHANGED, TicketStatus.IN_PROGRESS.name(), TicketStatus.RESOLVED.name());
        auditLogService.log(closed, manager, AuditAction.TICKET_CLOSED, TicketStatus.RESOLVED.name(), TicketStatus.CLOSED.name());

        TicketComment comment = new TicketComment();
        comment.setTicket(assigned);
        comment.setAuthor(agent);
        comment.setBody("Certificate renewal is in progress and expected to complete today.");
        commentRepository.save(comment);
        auditLogService.log(assigned, agent, AuditAction.COMMENT_ADDED, null, "seed-comment");
    }

    private User user(String email, String fullName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setActive(true);
        return user;
    }

    private Ticket ticket(String title, String description, TicketStatus status, TicketPriority priority, User requester, User assignee, Instant dueAt) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setRequester(requester);
        ticket.setAssignee(assignee);
        ticket.setDueAt(dueAt);
        return ticket;
    }
}
