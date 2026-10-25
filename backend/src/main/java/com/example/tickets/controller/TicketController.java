package com.example.tickets.controller;

import com.example.tickets.dto.AssignTicketRequest;
import com.example.tickets.dto.ChangeTicketStatusRequest;
import com.example.tickets.dto.CreateCommentRequest;
import com.example.tickets.dto.CreateTicketRequest;
import com.example.tickets.dto.TicketAuditLogResponse;
import com.example.tickets.dto.TicketCommentResponse;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.dto.UpdateTicketRequest;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.exception.BadRequestException;
import com.example.tickets.security.CurrentUser;
import com.example.tickets.service.TicketService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final CurrentUser currentUser;

    public TicketController(TicketService ticketService, CurrentUser currentUser) {
        this.ticketService = ticketService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(currentUser.get(), request);
    }

    @GetMapping
    public Page<TicketResponse> listTickets(
        @RequestParam(required = false) TicketStatus status,
        @RequestParam(required = false) TicketPriority priority,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) Long requesterId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) {
            throw new BadRequestException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new BadRequestException("size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ticketService.listTickets(currentUser.get(), status, priority, assigneeId, requesterId, createdAfter, createdBefore, search, pageable);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(currentUser.get(), id);
    }

    @PatchMapping("/{id}")
    public TicketResponse updateTicket(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest request) {
        return ticketService.updateTicket(currentUser.get(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(currentUser.get(), id);
    }

    @PostMapping("/{id}/assign")
    public TicketResponse assignTicket(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assignTicket(currentUser.get(), id, request);
    }

    @PostMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeTicketStatusRequest request) {
        return ticketService.changeStatus(currentUser.get(), id, request);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest request) {
        return ticketService.addComment(currentUser.get(), id, request);
    }

    @GetMapping("/{id}/comments")
    public List<TicketCommentResponse> listComments(@PathVariable Long id) {
        return ticketService.listComments(currentUser.get(), id);
    }

    @GetMapping("/{id}/audit-logs")
    public List<TicketAuditLogResponse> listAuditLogs(@PathVariable Long id) {
        return ticketService.listAuditLogs(currentUser.get(), id);
    }
}

