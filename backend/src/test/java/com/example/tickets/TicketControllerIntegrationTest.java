package com.example.tickets;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tickets.entity.Role;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;

@EnabledIfSystemProperty(named = "runTestcontainers", matches = "true")
@Testcontainers(disabledWithoutDocker = true)
class TicketControllerIntegrationTest extends IntegrationTestSupport {
    @Test
    void requesterCanCreateTicket() throws Exception {
        User requester = user("requester@example.com", Role.REQUESTER);

        mockMvc.perform(post("/tickets")
                .header("Authorization", token(requester))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Cannot access dashboard",
                      "description": "Dashboard fails after login",
                      "priority": "HIGH"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.requester.email").value("requester@example.com"));
    }

    @Test
    void listTicketsSupportsPaginationAndStatusFilter() throws Exception {
        User requester = user("requester@example.com", Role.REQUESTER);
        ticket(requester, null, TicketStatus.OPEN, TicketPriority.HIGH);
        ticket(requester, null, TicketStatus.CLOSED, TicketPriority.LOW);

        mockMvc.perform(get("/tickets?status=OPEN&page=0&size=10")
                .header("Authorization", token(requester)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void commentCreationWritesAuditLog() throws Exception {
        User requester = user("requester@example.com", Role.REQUESTER);
        var ticket = ticket(requester, null, TicketStatus.OPEN, TicketPriority.MEDIUM);

        mockMvc.perform(post("/tickets/{id}/comments", ticket.getId())
                .header("Authorization", token(requester))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"body\":\"Please prioritize this.\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Please prioritize this."));

        mockMvc.perform(get("/tickets/{id}/audit-logs", ticket.getId())
                .header("Authorization", token(requester)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].action").value("COMMENT_ADDED"));
    }
}
