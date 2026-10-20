package dev.gaphunter.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.gaphunter.orders.api.OrderController;
import dev.gaphunter.orders.config.JacksonConfig;
import dev.gaphunter.orders.domain.Order;
import dev.gaphunter.orders.domain.OrderService;
import dev.gaphunter.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(JacksonConfig.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService service;

    @Test
    void rejectsAnOrderWithoutCustomer() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_name\": \" \", \"total\": 10}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(), any());
    }

    @Test
    void rejectsANegativeTotal() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_name\": \"Acme\", \"total\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsAValidOrder() throws Exception {
        given(service.create(eq("Acme"), any(BigDecimal.class))).willReturn(
                new Order("Acme", new BigDecimal("10.00"), OrderStatus.NEW, OffsetDateTime.parse("2026-10-01T00:00Z")));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customer_name\": \"Acme\", \"total\": 10}"))
                .andExpect(status().isCreated());
    }
}
