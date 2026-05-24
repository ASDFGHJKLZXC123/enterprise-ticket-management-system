package com.example.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.tickets.dto.CountResponse;
import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.service.DashboardService;
import com.example.tickets.service.TicketService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    TicketRepository ticketRepository;

    @Mock
    TicketService ticketService;

    @Test
    void summaryReturnsCountsAndRecentTickets() {
        DashboardService dashboardService = new DashboardService(ticketRepository, ticketService);
        User manager = user(2L, Role.MANAGER);
        Ticket recent = ticket(10L, manager);
        Specification<Ticket> visible = (root, query, cb) -> cb.conjunction();

        when(ticketService.visibleTo(manager)).thenReturn(visible);
        when(ticketRepository.findAll(eq(visible), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(recent)));
        when(ticketRepository.count(any(Specification.class))).thenReturn(5L, 2L, 1L, 1L, 1L, 3L, 1L);

        var summary = dashboardService.summary(manager);

        assertThat(summary.totalTickets()).isEqualTo(5);
        assertThat(summary.openTickets()).isEqualTo(2);
        assertThat(summary.overdueTickets()).isEqualTo(1);
        assertThat(summary.recentTickets()).hasSize(1);
    }

    @Test
    void statusCountsIncludeAllWorkflowStates() {
        DashboardService dashboardService = new DashboardService(ticketRepository, ticketService);
        User manager = user(2L, Role.MANAGER);
        Specification<Ticket> visible = (root, query, cb) -> cb.conjunction();
        when(ticketService.visibleTo(manager)).thenReturn(visible);
        when(ticketRepository.count(any(Specification.class))).thenReturn(3L, 2L, 1L, 4L, 0L);

        List<CountResponse> counts = dashboardService.ticketsByStatus(manager);

        assertThat(counts).extracting(CountResponse::key)
            .containsExactly("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED", "REOPENED");
        assertThat(counts).extracting(CountResponse::count)
            .containsExactly(3L, 2L, 1L, 4L, 0L);
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

    private Ticket ticket(Long id, User requester) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Ticket " + id);
        ticket.setDescription("Description");
        ticket.setRequester(requester);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);
        return ticket;
    }
}

