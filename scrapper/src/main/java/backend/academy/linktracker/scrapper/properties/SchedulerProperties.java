package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.scheduler")
public record SchedulerProperties(
        Duration linkUpdateDelay,
        @Positive int batchSize,
        @Positive int threads,
        @PositiveOrZero int offset) {}
