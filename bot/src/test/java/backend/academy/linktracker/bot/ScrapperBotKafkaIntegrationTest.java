package backend.academy.linktracker.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.bot.service.notification.NotificationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Import(TestcontainersConfiguration.class)
class ScrapperBotKafkaIntegrationTest {

    @TestConfiguration
    static class KafkaProducerTestConfig {
        @Bean
        @Primary
        public ProducerFactory<Object, Object> testProducerFactory(
                KafkaProperties properties,
                @Value("${spring.kafka.properties.schema.registry.url:mock://localhost:8082}")
                        String schemaRegistryUrl) {
            Map<String, Object> config = properties.buildProducerProperties();
            config.put(
                    org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class);
            config.put(
                    org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    io.confluent.kafka.serializers.KafkaAvroSerializer.class);
            config.put("schema.registry.url", schemaRegistryUrl);
            return new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        @Primary
        public KafkaTemplate<Object, Object> testKafkaTemplate(ProducerFactory<Object, Object> factory) {
            return new KafkaTemplate<>(factory);
        }
    }

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @MockitoBean
    private NotificationService notificationService;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Test
    void scrapperSendsUpdate_botReceivesUpdate() {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(10L)
                .setUrl("https://github.com/test/repo")
                .setDescription("fpfpffppf.")
                .setTgChatIds(List.of(12345L))
                .build();
        kafkaTemplate.send(topicName, event.getUrl().toString(), event);
        verify(notificationService, timeout(10000)).processUpdate(any());
    }
}
