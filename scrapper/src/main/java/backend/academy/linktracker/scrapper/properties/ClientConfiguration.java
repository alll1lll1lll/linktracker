package backend.academy.linktracker.scrapper.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    private static final String GITHUB_BASE_URL = "https://api.github.com";
    private static final String STACKOVERFLOW_BASE_URL = "https://api.stackexchange.com/2.3";

    @Bean
    @Qualifier("githubRestClient")
    public RestClient githubRestClient(GithubProperties properties) {
        return RestClient.builder()
                .baseUrl(GITHUB_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .defaultHeader("User-Agent", "LinkTrackerBot-Application")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    @Bean
    @Qualifier("stackOverflowRestClient")
    public RestClient stackOverflowRestClient() {
        return RestClient.builder().baseUrl(STACKOVERFLOW_BASE_URL).build();
    }

    @Bean
    @Qualifier("botRestClient")
    public RestClient botRestClient(BotProperties properties) {
        return RestClient.builder().baseUrl(properties.url()).build();
    }
}
