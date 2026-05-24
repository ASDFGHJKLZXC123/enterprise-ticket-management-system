package com.example.tickets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class SecurityIntegrationTest extends IntegrationTestSupport {
    @Test
    void requesterCannotViewAnotherRequestersTicket() throws Exception {
        User owner = user("owner@example.com", Role.REQUESTER);
        User other = user("other@example.com", Role.REQUESTER);
        var ticket = ticket(owner, null, TicketStatus.OPEN, TicketPriority.HIGH);

        mockMvc.perform(get("/tickets/{id}", ticket.getId())
                .header("Authorization", token(other)))
            .andExpect(status().isForbidden());
    }

    @Test
    void agentCanViewAssignedTicket() throws Exception {
        User requester = user("requester@example.com", Role.REQUESTER);
        User agent = user("agent@example.com", Role.AGENT);
        var ticket = ticket(requester, agent, TicketStatus.OPEN, TicketPriority.HIGH);

        mockMvc.perform(get("/tickets/{id}", ticket.getId())
                .header("Authorization", token(agent)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignee.email").value("agent@example.com"));
    }

    @Test
    void managerCanAssignTicket() throws Exception {
        User requester = user("requester@example.com", Role.REQUESTER);
        User manager = user("manager@example.com", Role.MANAGER);
        User agent = user("agent@example.com", Role.AGENT);
        var ticket = ticket(requester, null, TicketStatus.OPEN, TicketPriority.HIGH);

        mockMvc.perform(post("/tickets/{id}/assign", ticket.getId())
                .header("Authorization", token(manager))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"assigneeId\":" + agent.getId() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignee.id").value(agent.getId()));
    }

    @Test
    void adminCanDeactivateUserAndInactiveUserCannotLogin() throws Exception {
        User admin = user("admin@example.com", Role.ADMIN);
        User target = user("target@example.com", Role.AGENT);

        mockMvc.perform(patch("/users/{id}/deactivate", target.getId())
                .header("Authorization", token(admin)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "target@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
}
