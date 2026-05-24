package com.example.tickets.controller;

import com.example.tickets.dto.CountResponse;
import com.example.tickets.dto.DashboardSummaryResponse;
import com.example.tickets.security.CurrentUser;
import com.example.tickets.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService, CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.currentUser = currentUser;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.summary(currentUser.get());
    }

    @GetMapping("/tickets-by-status")
    public List<CountResponse> ticketsByStatus() {
        return dashboardService.ticketsByStatus(currentUser.get());
    }

    @GetMapping("/tickets-by-priority")
    public List<CountResponse> ticketsByPriority() {
        return dashboardService.ticketsByPriority(currentUser.get());
    }

    @GetMapping("/tickets-by-assignee")
    public List<CountResponse> ticketsByAssignee() {
        return dashboardService.ticketsByAssignee(currentUser.get());
    }
}

