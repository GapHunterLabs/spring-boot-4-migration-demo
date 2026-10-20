package dev.gaphunter.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Over real HTTP, on its own database so the orders it creates don't leak into the contract test. */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:orders-http;DB_CLOSE_DELAY=-1")
class OrderApiHttpTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void unknownOrderIs404() {
        ResponseEntity<String> response = rest.getForEntity("/api/orders/999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createdOrderCanBeReadBack() {
        ResponseEntity<Map> created = rest.postForEntity(
                "/api/orders", Map.of("customer_name", "Acme", "total", 42), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> read = rest.getForEntity(created.getHeaders().getLocation(), Map.class);

        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(read.getBody()).containsEntry("customer_name", "Acme");
    }
}
