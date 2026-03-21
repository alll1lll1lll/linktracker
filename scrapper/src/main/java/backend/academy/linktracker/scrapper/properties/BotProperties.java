package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.bot")
public record BotProperties(@NotBlank String url) {}
