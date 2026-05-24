package com.example.tickets;

import com.example.tickets.entity.Role;
import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import com.example.tickets.entity.User;
import com.example.tickets.repository.TicketAuditLogRepository;
import com.example.tickets.repository.TicketCommentRepository;
import com.example.tickets.repository.TicketRepository;
import com.example.tickets.repository.UserRepository;
import com.example.tickets.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class IntegrationTestSupport {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("tickets_test")
        .withUsername("tickets")
        .withPassword("tickets");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketCommentRepository commentRepository;

    @Autowired
    TicketAuditLogRepository auditLogRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.seed.enabled", () -> "false");
        registry.add("app.jwt.secret", () -> "test-secret-that-is-long-enough-for-hmac-signing");
    }

    @BeforeEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    User user(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(role.name() + " User");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    Ticket ticket(User requester, User assignee, TicketStatus status, TicketPriority priority) {
        Ticket ticket = new Ticket();
        ticket.setTitle("Test ticket");
        ticket.setDescription("Integration test ticket");
        ticket.setRequester(requester);
        ticket.setAssignee(assignee);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        return ticketRepository.save(ticket);
    }

    String token(User user) {
        return "Bearer " + jwtTokenProvider.createToken(user);
    }
}
