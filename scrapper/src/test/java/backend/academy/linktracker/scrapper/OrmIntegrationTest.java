package backend.academy.linktracker.scrapper;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class OrmIntegrationTest extends AbstractIntegrationTest {
    @DynamicPropertySource
    static void ormProperties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "orm");
    }
}
