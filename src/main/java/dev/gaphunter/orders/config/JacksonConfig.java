package dev.gaphunter.orders.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The public API uses snake_case field names: existing clients (a billing system and a mobile app) parse them.
 */
@Configuration
public class JacksonConfig {

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
