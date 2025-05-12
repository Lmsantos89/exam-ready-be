package com.lms.examready;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ExamReadyApplicationTests {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("examready")
            .withUsername("examready_user")
            .withPassword("examready_pass")
            .withReuse(true);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            String r2dbcUrl = "r2dbc:postgresql://" +
                    postgres.getHost() + ":" +
                    postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) +
                    "/" + postgres.getDatabaseName();

            registry.add("spring.r2dbc.url", () -> r2dbcUrl);
            registry.add("spring.r2dbc.username", postgres::getUsername);
            registry.add("spring.r2dbc.password", postgres::getPassword);

            registry.add("spring.flyway.url", postgres::getJdbcUrl);
            registry.add("spring.flyway.user", postgres::getUsername);
            registry.add("spring.flyway.password", postgres::getPassword);
        }
    }

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

}
