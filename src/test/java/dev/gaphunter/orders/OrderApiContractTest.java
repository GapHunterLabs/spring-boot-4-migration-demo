package dev.gaphunter.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Byte-for-byte checks of what existing clients receive. Field names, escaping and number formats are part of the
 * contract, so the comparison is on the raw body, not on parsed JSON.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void orderListMatchesTheContract() throws Exception {
        String body = mockMvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertMatchesContract(body, "orders.json");
    }

    @Test
    void csvExportMatchesTheContract() throws Exception {
        String body = mockMvc.perform(get("/api/orders/export").accept("text/csv"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertMatchesContract(body, "orders.csv");
    }

    private static void assertMatchesContract(String actual, String contractFile) throws IOException {
        String expected;
        try (InputStream in = OrderApiContractTest.class.getResourceAsStream("/contract/" + contractFile)) {
            assertThat(in).as("contract file " + contractFile).isNotNull();
            expected = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (!expected.strip().equals(actual.strip())) {
            Path out = Path.of("build", "contract-actual", contractFile);
            Files.createDirectories(out.getParent());
            Files.writeString(out, actual);
        }
        assertThat(actual.strip()).isEqualTo(expected.strip());
    }
}
