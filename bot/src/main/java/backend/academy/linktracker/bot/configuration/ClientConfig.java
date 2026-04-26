package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.GrpcScrapperClient;
import backend.academy.linktracker.bot.client.RestScrapperClient;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.mapper.GrpcMapper;
import backend.academy.linktracker.bot.properties.AppProperties;
import backend.academy.linktracker.grpc.ScrapperServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClientConfig {

    private final AppProperties properties;

    @Bean
    @ConditionalOnProperty(name = "app.client-type", havingValue = "rest")
    public ScrapperClient restScrapperClient() {
        String baseUrl = properties.scrapper().url();
        log.info("Creating REST Scrapper Client pointing to: {}", baseUrl);
        return new RestScrapperClient(RestClient.builder().baseUrl(baseUrl).build());
    }

    @Bean
    @ConditionalOnMissingBean(ScrapperClient.class)
    public ScrapperClient grpcScrapperClient(GrpcMapper mapper) {
        String address = properties.scrapper().grpcAddress();
        log.info("Creating fallback gRPC Scrapper Client for commands pointing to: {}", address);

        var channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        return new GrpcScrapperClient(ScrapperServiceGrpc.newBlockingStub(channel), mapper);
    }
}
