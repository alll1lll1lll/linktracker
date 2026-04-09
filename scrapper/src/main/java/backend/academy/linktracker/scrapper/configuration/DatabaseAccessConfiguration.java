package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcChatRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTagRepository;
import backend.academy.linktracker.scrapper.repository.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.orm.OrmSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.orm.OrmTagRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class DatabaseAccessConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc", matchIfMissing = true)
    public static class JdbcAccessConfiguration {

        @Bean
        public ChatRepository chatRepository(JdbcClient jdbcClient) {
            return new JdbcChatRepository(jdbcClient);
        }

        @Bean
        public LinkRepository linkRepository(JdbcClient jdbcClient) {
            return new JdbcLinkRepository(jdbcClient);
        }

        @Bean
        public SubscriptionRepository subscriptionRepository(JdbcClient jdbcClient) {
            return new JdbcSubscriptionRepository(jdbcClient);
        }

        @Bean
        public TagRepository tagRepository(JdbcClient jdbcClient) {
            return new JdbcTagRepository(jdbcClient);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "orm")
    public static class JpaAccessConfiguration {

        @Bean
        public ChatRepository chatRepository(EntityManager entityManager) {
            return new OrmChatRepository(entityManager);
        }

        @Bean
        public LinkRepository linkRepository(EntityManager entityManager) {
            return new OrmLinkRepository(entityManager);
        }

        @Bean
        public SubscriptionRepository subscriptionRepository(EntityManager entityManager) {
            return new OrmSubscriptionRepository(entityManager);
        }

        @Bean
        public TagRepository tagRepository(EntityManager entityManager) {
            return new OrmTagRepository(entityManager);
        }
    }
}
