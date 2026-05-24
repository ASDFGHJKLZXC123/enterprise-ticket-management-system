package com.example.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tickets.dto.AssignTicketRequest;
import com.example.tickets.dto.ChangeTicketStatusRequest;
import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.UpdateTicketRequest;
import com.example.tickets.entity.AuditAction;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.exception.BadRequestException;
import com.example.tickets.exception.ForbiddenException;
import com.example.tickets.repository.TicketAuditLogRepository;
import com.example.tickets.repository.TicketCommentRepository;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.repository.UserRepository;
import com.example.tickets.service.AuditLogService;
import com.example.tickets.service.TicketService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock
    TicketRepository ticketRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TicketCommentRepository commentRepository;

    @Mock
    TicketAuditLogRepository auditLogRepository;

    @Mock
    AuditLogService auditLogService;

    TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, userRepository, commentRepository, auditLogRepository, auditLogService);
    }

    @Test
    void createsOpenTicketAndAuditLog() {
        User requester = user(1L, Role.REQUESTER);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ticketService.createTicket(requester, new CreateTicketRequest(
            "VPN down",
            "Cannot connect to VPN",
            TicketPriority.HIGH,
            Instant.now().plusSeconds(3600)
        ));

        assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.requester().id()).isEqualTo(requester.getId());
        verify(auditLogService).log(any(Ticket.class), eq(requester), eq(AuditAction.TICKET_CREATED), eq(null), eq(TicketStatus.OPEN.name()));
    }

    @Test
    void managerCanAssignTicketToActiveAgent() {
        User manager = user(2L, Role.MANAGER);
        User agent = user(3L, Role.AGENT);
        Ticket ticket = ticket(10L, user(1L, Role.REQUESTER), null, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(agent));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ticketService.assignTicket(manager, 10L, new AssignTicketRequest(3L));

        assertThat(response.assignee().id()).isEqualTo(3L);
        verify(auditLogService).log(eq(ticket), eq(manager), eq(AuditAction.TICKET_ASSIGNED), eq("Unassigned"), eq("3"));
    }

    @Test
    void requesterCannotAssignTicket() {
        User requester = user(1L, Role.REQUESTER);

        assertThatThrownBy(() -> ticketService.assignTicket(requester, 10L, new AssignTicketRequest(3L)))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("assign");
    }

    @Test
    void assignedAgentCanMoveOpenTicketToInProgress() {
        User requester = user(1L, Role.REQUESTER);
        User agent = user(3L, Role.AGENT);
        Ticket ticket = ticket(10L, requester, agent, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ticketService.changeStatus(agent, 10L, new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS));

        assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(auditLogService).log(eq(ticket), eq(agent), eq(AuditAction.STATUS_CHANGED), eq("OPEN"), eq("IN_PROGRESS"));
    }

    @Test
    void invalidStatusTransitionIsRejected() {
        User requester = user(1L, Role.REQUESTER);
        User agent = user(3L, Role.AGENT);
        Ticket ticket = ticket(10L, requester, agent, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.changeStatus(agent, 10L, new ChangeTicketStatusRequest(TicketStatus.RESOLVED)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    void managerCanReopenClosedTicket() {
        User manager = user(2L, Role.MANAGER);
        Ticket ticket = ticket(10L, user(1L, Role.REQUESTER), user(3L, Role.AGENT), TicketStatus.CLOSED);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ticketService.changeStatus(manager, 10L, new ChangeTicketStatusRequest(TicketStatus.REOPENED));

        assertThat(response.status()).isEqualTo(TicketStatus.REOPENED);
        verify(auditLogService).log(eq(ticket), eq(manager), eq(AuditAction.TICKET_REOPENED), eq("CLOSED"), eq("REOPENED"));
    }

    @Test
    void sameStatusTransitionIsRejected() {
        User manager = user(2L, Role.MANAGER);
        Ticket ticket = ticket(10L, user(1L, Role.REQUESTER), null, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.changeStatus(manager, 10L, new ChangeTicketStatusRequest(TicketStatus.OPEN)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("already in status");
    }

    @Test
    void requesterCannotEditOwnTicketDetails() {
        User requester = user(1L, Role.REQUESTER);
        Ticket ticket = ticket(10L, requester, null, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.updateTicket(requester, 10L, new UpdateTicketRequest("New title", null, null, null)))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("edit ticket details");
    }

    @Test
    void agentWhoRequestedButIsNotAssignedCannotChangeStatus() {
        User agentRequester = user(3L, Role.AGENT);
        User assignedAgent = user(4L, Role.AGENT);
        Ticket ticket = ticket(10L, agentRequester, assignedAgent, TicketStatus.OPEN);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.changeStatus(agentRequester, 10L, new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("assigned agents");
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(role.name().toLowerCase() + id + "@example.com");
        user.setFullName(role.name() + " " + id);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Ticket ticket(Long id, User requester, User assignee, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Ticket " + id);
        ticket.setDescription("Description");
        ticket.setRequester(requester);
        ticket.setAssignee(assignee);
        ticket.setStatus(status);
        ticket.setPriority(TicketPriority.MEDIUM);
        return ticket;
    }
}
