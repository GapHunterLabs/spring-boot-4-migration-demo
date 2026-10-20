package dev.gaphunter.orders.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The public API uses snake_case field names: existing clients (a billing system and a mobile app) parse them.
 */
@Configuration
public class JacksonConfig {

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
