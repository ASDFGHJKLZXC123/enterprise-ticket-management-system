package com.example.tickets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tickets.controller.TicketController;
import com.example.tickets.dto.TicketResponse;
import com.example.tickets.entity.User;
import com.example.tickets.exception.GlobalExceptionHandler;
import com.example.tickets.security.CurrentUser;
import com.example.tickets.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// Controller contract only; database and security filters belong to integration tests.
@ExtendWith(MockitoExtension.class)
class TicketPaginationTest {
    @Mock
    TicketService ticketService;

    @Mock
    CurrentUser currentUser;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new TicketController(ticketService, currentUser))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @ParameterizedTest(name = "page={0}, size={1} returns 400")
    @CsvSource({"-1, 20", "0, 0", "0, -1"})
    void rejectsInvalidPagination(int page, int size) throws Exception {
        mockMvc.perform(get("/tickets")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest(name = "size={0} returns effective size={1}")
    @CsvSource({"1, 1", "101, 100"})
    void preservesValidPageSizeAndExistingCap(int requestedSize, int expectedSize) throws Exception {
        stubTicketList();

        mockMvc.perform(get("/tickets")
                .param("page", "0")
                .param("size", String.valueOf(requestedSize)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(expectedSize));
    }

    @Test
    void preservesDefaultPagination() throws Exception {
        stubTicketList();

        mockMvc.perform(get("/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(20));
    }

    private void stubTicketList() {
        when(currentUser.get()).thenReturn(new User());
        when(ticketService.listTickets(
            any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenAnswer(invocation -> Page.<TicketResponse>empty(invocation.getArgument(8)));
    }
}
