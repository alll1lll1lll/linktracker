package backend.academy.linktracker.scrapper.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Bean
    public NewTopic linkUpdatesTopic() {
        return TopicBuilder.name(topicName)
                .partitions(3) // 3, тк кластер из 3х брокеров
                .replicas(3) // аналогично
                .config("min.insync.replicas", "2") // кластер переживет если упадет 1 брокер
                .build();
    }
}
