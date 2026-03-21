package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestClientConfig {
    @Bean
    @Primary
    @Qualifier("githubRestClient")
    public RestClient githubRestClient() {
        return RestClient.builder().baseUrl("http://localhost:8080").build();
    }

    @Bean
    @Qualifier("stackOverflowRestClient")
    public RestClient stackOverflowRestClient() {
        return RestClient.builder().baseUrl("http://localhost:8080").build();
    }

    @Bean
    public StackoverflowProperties stackoverflowProperties() {
        StackoverflowProperties props = new StackoverflowProperties();
        props.setKey("key");
        props.setAccessToken("token");
        return props;
    }
}
