package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.client.interfaces.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("transportBotClient")
@ConditionalOnProperty(name = "app.client-type", havingValue = "kafka", matchIfMissing = true)
public class KafkaBotClient implements BotClient {

    private final KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;
    private final String topicName;

    public KafkaBotClient(
            KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate, @Value("${app.kafka.topic-name}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        log.atInfo().log("BotClient is active. Using: Kafka with Avro");
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        LinkUpdateEvent avroEvent = LinkUpdateEvent.newBuilder()
                .setId(update.getId())
                .setUrl(update.getUrl().toString())
                .setDescription(update.getDescription())
                .setTgChatIds(update.getTgChatIds())
                .build();

        try {
            kafkaTemplate.send(topicName, update.getUrl().toString(), avroEvent).get();
            log.atInfo().log("successfully sent notification to Kafka. URL: {}", update.getUrl());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("kafka delivery failed", e);
        }
    }
}
