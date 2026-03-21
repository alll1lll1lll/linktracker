package backend.academy.linktracker.bot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String clientType, ScrapperConfig scrapper) {
    public record ScrapperConfig(String url, String grpcAddress) {}
}
