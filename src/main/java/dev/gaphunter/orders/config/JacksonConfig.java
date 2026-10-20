package dev.gaphunter.orders.config;

import java.time.Clock;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;

/**
 * The public API uses snake_case field names: existing clients (a billing system and a mobile app) parse them.
 *
 * <p>Spring Boot 4 only uses a {@code JsonMapper} it builds itself (or a {@code JsonMapper} bean), so the naming is
 * applied as a customizer of that builder. The {@code spring.jackson.*} properties keep applying on top.
 */
@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer snakeCaseFieldNames() {
        return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
