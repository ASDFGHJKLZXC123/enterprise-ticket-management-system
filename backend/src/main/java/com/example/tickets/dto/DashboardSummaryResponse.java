package com.example.tickets.dto;

import java.util.List;

public record DashboardSummaryResponse(
    long totalTickets,
    long openTickets,
    long inProgressTickets,
    long resolvedTickets,
    long closedTickets,
    long highPriorityTickets,
    long overdueTickets,
    List<TicketResponse> recentTickets
) {
}

